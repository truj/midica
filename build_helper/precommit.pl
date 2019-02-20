#!/usr/bin/perl -w

# This Source Code Form is subject to the terms of the
# Mozilla Public License, v. 2.0. 
# If a copy of the MPL was not distributed with this file,
# You can obtain one at http://mozilla.org/MPL/2.0/.

###############################################################################
# This is supposed to be called from the actual precommit hook for Midica.
# Start it from the real precommit hook (.git/hooks/pre_commit)
# with the following lines (assuming it's a bourne shell script):
# 
# project_path=/path/to/project/midica
# create_jar=1
# create_javadoc=1
# javadoc_path=/path/to/javadoc
# exec perl -w -CSD -Mutf8 "$project_path/build_helper/precommit.pl" "$project_path" "$create_jar" "$create_javadoc" "$javadoc_path"
# 
# If you don't want to start it as the last command of your precommit hook
# than you have to evaluate the exit status in .git/hooks/pre_commit
# yourself.
# 
# This script performs the following tasks:
# - Calculating a new minor version from the current time and writing it
#   into the according variable in Midica.java.
# - Compiling the source to a temporary directory and creating a jar
#   file from it. Deleting old jar files.
#   (Only if the env variable 'create_jar' is set.)
# - Creating new Javadoc
#   (Only if the env variables 'create_javadoc' and 'javadoc_path' are both set.)
###############################################################################

use strict;
use File::Copy qw(copy);
use File::Path qw(make_path remove_tree);

# get project path from command line
my $project_path        = shift;
my $must_create_jar     = shift;
my $must_create_javadoc = shift;
my $javadoc_path        = shift;

# check if anything is going to be committed at all.
my $output    = `git status -z --porcelain '$project_path'`;
my @files     = split /\0/, $output;
my $is_commit = 0;
FILE:
foreach my $file (@files) {
	my $staging_flag = substr $file, 0, 1;
	next if ' ' eq $staging_flag || '?' eq $staging_flag || '!' eq $staging_flag;
	$is_commit = 1;
	last FILE;
}
if (!$is_commit) {
	die "No files staged. Nothing to commit.\n";
}

# check if Midica.java is clean (either fully staged or not modified at all).
my $java_file = $project_path . '/src/org/midica/Midica.java';
my $is_clean  = 1;
$output = `git status -z --porcelain '$java_file'`;
if ($output) {
	my $modified_flag = substr $output, 1, 1;
	
	# modified?
	if ('M' eq $modified_flag) {
		$is_clean = 0;
	}
}
if (!$is_clean) {
	die "Midica.java is modified but not fully staged.\n";
}

# execute unit tests
my $junit_jar = $project_path . '/build_helper/junit-platform-console-standalone-1.4.0.jar';
my $bin_path  = $project_path . '/bin';
my $cmd       = "java -jar '$junit_jar' --class-path '$bin_path' --scan-class-path";
my $status    = system $cmd;
if ($status) {
	die "Unit tests failed (using the following command):\n"
	  . "$cmd\n";
}

# get major and minor version number
my $major_version = -1;
open my $fh, '<', $java_file or die "Cannot read $java_file: $!\n";
while (my $line = <$fh>) {
	if ($line =~ /(\bint\s+VERSION_MAJOR\s*=)\s*(\d+)/) {
		$major_version =  $2;
		last;
	}
}
close $fh or die "Cannot close $java_file: $!\n";
if ($major_version < 0) {
	die "Did not find major version in $java_file.\n";
}
my $minor_version = time();
my $version       = $major_version . '.' . $minor_version;

# Build up the command to replace VERSION_MINOR in Midica.java.
# 1. perl and options and opening single quote (') for the regex
# 2. the regex
# 3. closing single quote for the regex and single-quoted file parameter
$cmd = "perl -CSD -Mutf8 -p -i -e '"
        . 's/(\bint\s+VERSION_MINOR\s*=)\s*(\d+)/\1 ' . $minor_version . '/'
        . "' '$java_file'";

# execute the replacement command
my $status = system $cmd;
if ($status) {
	die "Could not replace minor version in $java_file.\n"
	  . "Command failed: $cmd\n";
}

# add Midica.java to the staging area
$cmd    = "git add '$java_file'";
$status = system $cmd;
if ($status) {
	die "Command failed: $cmd\n";
}

# delete old jar file(s) and create a new one, if configured
if ($must_create_jar) {
	
	# (delete and re)create temp target and resource path
	my $tmp_path = $project_path . '/tmp_target';
	my $tmp_res  = $tmp_path . '/org/midica/resources';
	remove_tree($tmp_path);
	mkdir($tmp_path)    or die "Cannot create $tmp_path: $!\n";
	make_path($tmp_res) or die "Cannot create $tmp_res: $!\n";
	
	# copy resources into temp dir
	my $res_dir   = $project_path . '/src/org/midica/resources';
	my @resources = glob $res_dir . '/*';
	if (!@resources) {
		print "Cannot read resources directory $res_dir\n";
	}
	foreach my $file (@resources) {
		copy($file, $tmp_res) or die "Copy of resource failed: $!\n";
	}
	
	# compile classes into temp dir
	my $src_path = $project_path . '/src';
	my $cmd      = "javac -source 8 -target 8 -sourcepath '$src_path' -d '$tmp_path' '$java_file'";
	$status      = system $cmd;
	if ($status) {
		die "Command failed: $cmd\n";
	}
	
	# delete old jar files
	my @old_jars = glob $project_path . '/midica-*.jar';
	foreach my $file (@old_jars) {
		$status = system "git rm -f '$file'"; # delete from git and file system
		if ($status) {
			# probably not in the repository - only remove from file system
			unlink $file or die "Delete of '$file' failed: $!\n";
		}
	}
	
	# create new jar file
	my $jar_path      = $project_path . '/midica-' . $version . '.jar';
	my $manifest_path = $project_path . '/build_helper/manifest';
	$cmd              = "jar -cvfm '$jar_path' '$manifest_path' -C '$tmp_path' org";
	$status           = system $cmd;
	if ($status) {
		die "Command failed: $cmd\n";
	}
	
	# remove temp class files
	remove_tree($tmp_path) or die "Recursive delete of '$tmp_path' failed: $!\n";
	
	# add jar file to the staging area
	$cmd    = "git add '$jar_path'";
	$status = system $cmd;
	if ($status) {
		die "Command failed: $cmd\n";
	}
}

# recreate javadoc
if ($must_create_javadoc && $javadoc_path) {
	
	# delete old javadoc
	my @old_files = glob $javadoc_path . '/*';
	foreach my $file (@old_files) {
		if (-d $file) {
			remove_tree($file) or die "Recursive delete of '$file' failed: $!\n";
		}
		else {
			unlink $file or die "Delete of '$file' failed: $!\n";
		}
	}
	
	# recreate javadoc
	my $doclet_path   = $project_path . '/build_helper/pegdown-doclet-1.3-all.jar';
	my $doclet_name   = 'ch.raffael.doclets.pegdown.PegdownDoclet';
	my $source_path   = $project_path . '/src';
	my $overview_path = $project_path . '/build_helper/overview.html';
	my $cmd = "javadoc -docletpath '$doclet_path'"
	        . " -doclet $doclet_name"
	        . " -d '$javadoc_path'"
	        . " -sourcepath '$source_path'"
	        . " -subpackages org.midica"
	        . " -overview '$overview_path'"
	        . " -private"
	        . " -encoding utf-8"
	        . " -author"
	        . " -charset utf-8"
	        . " -keywords"
	        . " -notimestamp"
	        . " -windowtitle 'Midica $version - Javadoc'"
	        . " -doctitle 'Midica - the MIDI Processing and Programming Tool'"
	        . " -header '<a href=\"http://www.midica.org/\" target=\"_top\"><b>Midica</b></a><br>$version'"
	;
	my $status = system $cmd;
	if ($status) {
		die "Could not create javadoc.\n"
		  . "Command failed: $cmd\n";
	}
}

sleep 1;

exit 0;
