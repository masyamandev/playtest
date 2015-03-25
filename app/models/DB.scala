package models

import sorm._

object DB extends Instance (
  entities = Seq(
    Entity[User](),
    Entity[Role](unique = Set(Seq("name"))),
    Entity[Phonebook](),
    Entity[Person](),
    Entity[Contact]()
  ),
//  url = "jdbc:h2:mem:test"
  url = "jdbc:mysql://localhost/playdb",
  user = "root"
)