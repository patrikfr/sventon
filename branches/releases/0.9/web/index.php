<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
  <head>
    <title>sventon - subversion repository browser</title>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
    <link rel="stylesheet" type="text/css" href="sventon.css">
  </head>

  <body bgcolor="#FFFFFF">
    <p>&nbsp;</p>

<?php
  // get the value of the request parameter
  $pageparameter=$_GET['page'];

  switch ($pageparameter) {
    case "history":
      $includepage="history.php";
      break;
    case "downloads":
      $includepage="downloads.php";
      break;
    case "faq":
      $includepage="faq.php";
      break;
    case "roadmap":
      $includepage="roadmap.php";
      break;
    case "links":
      $includepage="links.php";
      break;
    case "features":
      $includepage="features.php";
      break;
    case "screenshots":
      $includepage="screenshots.php";
      break;
    default:
      $includepage="about.php";
      break;
  }
?>

    <table width="100%" border="0" cellspacing="0">
      <tr>
        <td width="20%">&nbsp;</td>
        <td width="2%">&nbsp;</td>
        <td>&nbsp;</td>
        <td width="2%" style="background-color: #dddddd; border-right: black solid 1px;">&nbsp;</td>
        <td width="20%">&nbsp;</td>
       </tr>
      <tr>
        <td width="20%">&nbsp;</td>
        <td width="2%">&nbsp;</td>
        <td align="center"><img src="img/sventon.png" alt="sventon logo"/><br/>the pure java subversion repository browser</td>
        <td width="2%" style="background-color: #dddddd; border-right: black solid 1px;">&nbsp;</td>
        <td width="20%">&nbsp;</td>
       </tr>
      <tr style="background-color: #dddddd;">
        <td style="border-bottom: black solid 1px;">&nbsp;</td>
        <td style="border-bottom: black solid 1px;">&nbsp;</td>
        <td style="border-bottom: black solid 1px;">&nbsp;</td>
        <td style="background-color: #dddddd;">&nbsp;</td>
        <td style="border-bottom: black solid 1px; border-right: black solid 1px;">&nbsp;</td>
       </tr>
      <tr>
        <td align="right" valign="top">
          <table border="0" cellspacing="1" cellpadding="1">
            <tr>
              <td align="right">&nbsp;</td>
            </tr>
            <tr>
              <td align="right">
                <a href="index.php?page=about">[about]</a>
              </td>
            </tr>
            <tr>
              <td align="right">
                <a href="index.php?page=history">[change history]</a>
              </td>
            </tr>
            <tr>
              <td align="right">
                <a href="index.php?page=features">[features]</a>
              </td>
            </tr>
            <tr>
              <td align="right">
                <a href="index.php?page=screenshots">[screenshots]</a>
              </td>
            </tr>
            <tr>
              <td align="right">
                <a href="index.php?page=downloads">[downloads]</a>
              </td>
            </tr>
            <tr>
              <td align="right">
                <a href="index.php?page=faq">[faq]</a>
              </td>
            </tr>
            <tr>
              <td align="right">
                <a href="index.php?page=links">[links]</a>
              </td>
            </tr>
            <tr>
              <td align="right">
                <a href="index.php?page=roadmap">[roadmap]</a>
              </td>
            </tr>
            <tr>
              <td align="right">
                <a href="http://developer.berlios.de/mail/?group_id=3670">[mailinglist]</a>
              </td>
            </tr>
            <tr>
              <td align="right">
                <a href="http://developer.berlios.de/forum/?group_id=3670">[forum]</a>
              </td>
            </tr>
            <tr>
              <td align="right">
                <a href="http://developer.berlios.de/projects/sventon/">[project page]</a>
              </td>
            </tr>
          </table>
        </td>
        <td>&nbsp;</td>
        <td valign="top"><?php require($includepage); ?></td>
        <td style="background-color: #dddddd; border-right: black solid 1px;">&nbsp;</td>
        <td>&nbsp;</td>
      </tr>
      <tr>
        <td align="right" valign="bottom">
          <br/>
          <br/>
          <a href="http://developer.berlios.de" title="BerliOS Developer">
            <img src="http://developer.berlios.de/bslogo.php?group_id=3670" width="124px" height="32px" border="0" alt="BerliOS Developer Logo">
          </a>
          <br/>
        </td>
        <td>&nbsp;</td>
        <td>&nbsp;</td>
        <td style="background-color: #dddddd; border-bottom: black solid 1px; border-right: black solid 1px;">&nbsp;</td>
        <td>&nbsp;</td>
      </tr>
    </table>
<script src="http://www.google-analytics.com/urchin.js" type="text/javascript">
</script>
<script type="text/javascript">
_uacct = "UA-206174-2";
urchinTracker();
</script>
</body>
</html>
