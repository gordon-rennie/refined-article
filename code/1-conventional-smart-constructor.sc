def validateUser(username: String): Either[String, String] =
    if (username.isEmpty)
      Left("Username cannot be empty")
    else if (username.length > 20)
      Left("Username must be 20 characters or less")
    else if (!username.forall(_.isLetterOrDigit))
      Left("Usernames can only contain alphanumeric characters")
    else
      Right(username)

validateUser("xX_ŚePhI®OtH_Xx")

validateUser("Luna")