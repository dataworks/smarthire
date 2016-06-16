package applicant.etl

/**
 * LinkParser contains methods that parse different bits of info from a URL
 */
object LinkParser {
  /**
   * Will clean up a raw url in order to get a link to the user's profile picture
   *
   * @param rawUrl The url pulled straight from the user's resume
   * @param prefix  A string to be preapended to the profile
   * @return the github profile name with a possible prefix URL
   */
  def parseGithubProfile(prefix: String = "", rawUrl: String): Option[String] = {
    rawUrl match {
      case url if url.startsWith("https://github.com/") =>
        var slashCount = 0
        val urlBuilder = new StringBuilder()

        var slashedUrl = url.substring(19)

        if (!slashedUrl.endsWith("/")) {
          slashedUrl += "/"
        }

        //Add the prefix
        urlBuilder.append(prefix)

        //Grab each character up to the slash
        for (c <- slashedUrl; if slashCount < 1) {
          if (c.equals('/')) {
            slashCount += 1
          }
          else {
            urlBuilder.append(c)
          }
        }

        return Some(urlBuilder.toString())
      case _ =>
        return None
    }
  }
}
