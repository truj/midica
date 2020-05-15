# For Developers
The directory `build_helper` is only needed for Midica developers.
Normal users don't need to go on reading.

## `precommit.pl`
This is the heart of the development tools in this directory. It's a Perl script designed
to be used from GIT's precommit hook.

What precommit.pl does:
- Executing the unit tests (JUnit 5)
- Injecting a new minor version number, commit time and branch name into the source code (Midica.java)
- Updating the old jar file with a new version (inside of the repository)
- Deleting the old Javadoc and creating a new version (outside of the repository)

The first two of these tasks are always done. The other two are optional.
The script is configured with the following environment variables:
- `project_path` - absolute path to your repository
- `create_jar` - determins if the old jar file shall be replaced by a new one
- `create_javadoc` - determins if the old Javadoc files shall be replaced by new ones
- `javadoc_path` - absolute path to the Javadoc directory to be replaced, if `create_javadoc` is set

System requirements for using precommit.pl:
- **Perl**
- **Bash-like shell** (Or you have to set the environment variables differently in the precommit hook)
- **Unix-like operating system**
- **Java Development Kit 1.8 or higher**

Besides you need the project to be configured the following way:
- `src` and `test` must be configured as source directories.
- `bin` must be configured as output folder for class files (both from `src` and `test`).

You can embed `precommit.pl` into your precommit hook like this:

	project_path=/path/to/project/midica
	create_jar=1
	create_javadoc=1
	javadoc_path=/path/to/javadoc
	exec perl -w -CSD -Mutf8 "$project_path/build_helper/precommit.pl" "$project_path" "$create_jar" "$create_javadoc" "$javadoc_path"

## Javadoc
The Javadoc comments in this project are written in Markdown instead of HTML.
Markdown is a wiki-like language, much easier than HTML.
A rough description of Markdown can be found here:
http://daringfireball.net/projects/markdown/syntax
or here: http://en.wikipedia.org/wiki/Markdown

The appropriate doclet for producing API documentation in HTML can be found in the file **pegdown-doclet-1.3-all.jar**. This doclet is also used by **precommit.pl**.

If you don't use precommit.pl but still want to produce Javadoc with this doclet, you have to
add the following parameters to your `javadoc` command:
* `-docletpath /path/to/your/repository/build_helper/pegdown-doclet-1.3-all.jar`
* `-doclet ch.raffael.doclets.pegdown.PegdownDoclet`

In case you want to use a newer version of the doclet, you can find the according project here:
https://github.com/Abnaxos/pegdown-doclet

## Java Version
Currently the source is compliant with Java version 1.7 or higher.

However as a developer you need Java Development Kit 1.8 or higher in order to execute the unit tests as they are written in JUnit 5.
## Unit Tests

Given your output directory for class files is the **bin** folder, you can execute the unit tests like this:

`java -jar build_helper/junit-platform-console-standalone-1.4.0.jar --class-path bin --scan-class-path`

## Create JAR file
In order to create the jar file you can use the file `manifest`.

The command looks like this:

`jar -cvfm /path/to/midica.jar /repository/path/build_helper/manifest -C /path/to/midica/classes org`

