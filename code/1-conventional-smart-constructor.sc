final case class User private(username: String)

object User {
  def make(username: String): Either[String, User] =
    if (username.isEmpty)
      Left("Username cannot be empty")
    else if (username.length > 20)
      Left("Username must be 20 characters or less")
    else if (!username.forall(_.isLetterOrDigit))
      Left("Usernames can only contain alphanumeric characters")
    else
      Right(User(username))
}

User.make("xX_ŚePhI®OtH_Xx")

User.make("Luna")