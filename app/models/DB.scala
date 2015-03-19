package models

import sorm._

object DB extends Instance (
  entities = Seq(
    Entity[User](),
    Entity[Role](unique = Set(Seq("name")))
  ),
//  url = "jdbc:h2:mem:test"
  url = "jdbc:mysql://localhost/playdb",
  user = "root"
)