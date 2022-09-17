package bank.persistence

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

class AddressJDBCDAO(implicit datasource: Datasource) extends AddressDAO {

  private val createTblSql = """
                       |CREATE TABLE IF NOT EXISTS ADDRESS(
                       |  id                    bigint  NOT NULL  AUTO_INCREMENT PRIMARY KEY,
                       |  fk_customer_profile   bigint,
                       |  city                  varchar(50),
                       |  province              varchar(50),
                       |  town                  varchar(50),
                       |  street                varchar(100) NOT NULL,
                       |  lot                   varchar(10),
                       |  block                 varchar(10),
                       |  building_name         varchar(50),
                       |  apartment_number      varchar(10),
                       |  is_primary_address    boolean NOT NULL,
                       |  created_at            datetime DEFAULT CURRENT_TIMESTAMP;
                       |  UNIQUE (fk_customer_profile, city, province, town, street, lot, block, building_name, apartment_number)
                       |  FOREIGN KEY fk_customer_profile REFERENCES CUSTOMER_PROFILE(id)
                       |) engine=InnoDB DEFAULT CHARSET=utf8;
                       |""".stripMargin

  datasource.getConnection.createStatement().execute(createTblSql)

  override def getAddress(customerId: Int): Future[Seq[Address]] = Future {
    val conn = datasource.getConnection
    val statement = conn.prepareStatement(s"SELECT * FROM ADDRESS WHERE fk_customer_profile = ?")
    statement.setInt(1, customerId)
    val resultSet = statement.executeQuery()

    resultSet.first()
    var addresses: Seq[Address] = Nil

    do {
      val id = resultSet.getInt("id")
      val customerProfileId = Option(resultSet.getInt("fk_customer_profile"))
      val city = Option(resultSet.getString("city"))
      val province = Option(resultSet.getString("province"))
      val town = Option(resultSet.getString("town"))
      val street = resultSet.getString("street")
      val lot = Option(resultSet.getString("lot"))
      val block = Option(resultSet.getString("block"))
      val building_name = Option(resultSet.getString("building_name"))
      val apartment_number = Option(resultSet.getString("apartment_number"))
      val is_primary = resultSet.getBoolean("is_primary_address")
      val created_at = resultSet.getTimestamp("created_at")
      val address = Address(
        id,
        customerProfileId,
        city,
        province,
        town,
        street,
        lot,
        block,
        building_name,
        apartment_number,
        is_primary,
        created_at.toInstant
      )
      addresses = addresses :+ address
    } while(resultSet.next())

    addresses
  }

  override def saveAddress(address: WriteAddress): Future[Try[Int]] = Future {
    Try {
      val conn = datasource.getConnection
      conn.createStatement().execute("LOCK TABLE ADDRESS WRITE;")
      val statement = conn.prepareStatement(
        """
          |INSERT INTO ADDRESS
          |(fk_customer_profile, city, province, street, lot, block, building_name, apartment_number, is_primary_address)
          |VALUES (?, ?, ?, ?, ?, ?, ?, ?)
          |""".stripMargin)

      statement.setInt(1, address.fk_customer_profile)
      address.city.foreach(statement.setString(2, _))
      address.province.foreach(statement.setString(3, _))
      statement.setString(4, address.street)
      address.lot.foreach(statement.setString(5, _))
      address.block.foreach(statement.setString(6, _))
      address.building_name.foreach(statement.setString(7, _))
      address.apartment_number.foreach(statement.setString(8, _))
      statement.setBoolean(9, address.is_primary_address)
      statement.executeUpdate()

      val resultSet = conn.createStatement().executeQuery(s"SELECT MAX(id) as id FROM ADDRESS")
      conn.createStatement().execute("UNLOCK TABLES;")
      resultSet.getInt("id")
    }
  }
}
