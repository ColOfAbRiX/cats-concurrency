//  VERSIONS  //

lazy val AssemblyVersion       = "0.14.6"
lazy val BuildinfoVesion       = "0.9.0"
lazy val DynverVersion         = "4.0.0"
lazy val ErrorsSummaryVersion  = "0.6.3"
lazy val NativePackagerVersion = "1.3.25"
lazy val ReloadQuickVersion    = "1.0.0"
lazy val SbtStatsVersion       = "1.0.7"
lazy val ScalafmtVersion       = "2.3.0"
lazy val WartremoverVersion    = "2.4.3"

//  PLUGIN LIBRARIES  //

addSbtPlugin("com.dwijnand"     % "sbt-dynver"          % DynverVersion)
addSbtPlugin("com.dwijnand"     % "sbt-reloadquick"     % ReloadQuickVersion)
addSbtPlugin("com.eed3si9n"     % "sbt-assembly"        % AssemblyVersion)
addSbtPlugin("com.eed3si9n"     % "sbt-buildinfo"       % BuildinfoVesion)
addSbtPlugin("com.orrsella"     % "sbt-stats"           % SbtStatsVersion)
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % NativePackagerVersion)
addSbtPlugin("org.duhemm"       % "sbt-errors-summary"  % ErrorsSummaryVersion)
addSbtPlugin("org.scalameta"    % "sbt-scalafmt"        % ScalafmtVersion)
addSbtPlugin("org.wartremover"  % "sbt-wartremover"     % WartremoverVersion)
