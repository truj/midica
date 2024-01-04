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
# java_home=/path/to/java-installation/bin
# create_jar=1
# create_javadoc=1
# javadoc_path=/path/to/javadoc
# exec perl -w -CSD -Mutf8 "$project_path/build_helper/precommit.pl" "$project_path" "$java_home" "$create_jar" "$create_javadoc" "$javadoc_path"
# 
# If you don't want to start it as the last command of your precommit hook
# than you have to evaluate the exit status in .git/hooks/pre_commit
# yourself.
# 
# This script performs the following tasks:
# - Detecting or calculating the following variables:
#   - current branch name
#   - current UNIX timestamp
#   - current major version (from Midica.java).
#   - current minor version (from Midica.java).
# - Incrementing the minor version. (Only if we are in the master branch.)
# - Calculating the new full version.
# - Writing branch name, commit time and (updated) minor version into Midica.java.
# - Only if 'create_jar' is set:
#   - Compiling the source to a temporary directory and creating a jar
#     file from it.
#   - Updating midica.jar file.
# - Creating new Javadoc (using the calculated full version)
#   (Only in the master branch, and only if the env variables
#   'create_javadoc' and 'javadoc_path' are both set.)
###############################################################################

use strict;
use File::Copy qw(copy);
use File::Path qw(make_path remove_tree);

# get project path from command line
my $project_path        = shift;
my $java_home           = shift;
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
my $cmd       = "'$java_home/java' -jar '$junit_jar' --class-path '$bin_path' --scan-class-path";
my $status    = system $cmd;
if ($status) {
	die "Unit tests failed (using the following command):\n"
	  . "$cmd\n";
}

# get branch name
my $branch = `git rev-parse --abbrev-ref HEAD`;
chomp $branch;
if (! $branch) {
	die "Branch name not found.\n";
}
my $branch_suffix = 'master' eq $branch ? '' : '-' . $branch;

# get OLD major and minor version number
my $major_version  = undef;
my $middle_version = undef;
my $minor_version  = undef;
open my $fh, '<', $java_file or die "Cannot read $java_file: $!\n";
while (my $line = <$fh>) {
	if ($line =~ /(\bint\s+VERSION_MAJOR\s*=)\s*(\d+)/) {
		$major_version = $2;
	}
	elsif ($line =~ /(\bint\s+VERSION_MIDDLE\s*=)\s*(\d+)/) {
		$middle_version = $2;
	}
	elsif ($line =~ /(\bint\s+VERSION_MINOR\s*=)\s*(\-?\d+)/) {
		$minor_version = $2;
	}
	last if defined $major_version && defined $middle_version && defined $minor_version;
}
close $fh or die "Cannot close $java_file: $!\n";
if (! defined $major_version) {
	die "Did not find major version in $java_file.\n";
}
if (! defined $middle_version) {
	die "Did not find middle version in $java_file.\n";
}
if (! defined $minor_version) {
	die "Did not find minor version in $java_file.\n";
}

# get NEW version and commit time
if ('master' eq $branch) {
	$minor_version++;
}
my $commit_time = time();
my $version     = $major_version . '.' . $middle_version . '.' . $minor_version . $branch_suffix;

# Build up the commands to replace VERSION_MINOR, BRANCH and COMMIT_TIME in Midica.java.
# 1. perl and options and opening single quote (') for the regex
# 2. the regex
# 3. closing single quote for the regex and single-quoted file parameter
my $replace_cmd = "perl -CSD -Mutf8 -p -i -e '"
        . 's/(\b{{TYPE}}\s+{{NAME}}\s*=)\s*{{OLD}}/\1 {{NEW}}/'
        . "' '$java_file'";

# execute the replacement commands for VERSION_MINOR, BRANCH and COMMIT_TIME
my @replacements = (
	[ 'int',    'VERSION_MINOR', '(\-?\d+)',  $minor_version      ],
	[ 'String', 'BRANCH',        '"([^"]*)"', '"' . $branch . '"' ],
	[ 'int',    'COMMIT_TIME',   '(\d+)',     $commit_time        ],
);
foreach my $repl_ref (@replacements) {
	$cmd = $replace_cmd;
	$cmd =~ s/\{\{TYPE\}\}/$repl_ref->[0]/;
	$cmd =~ s/\{\{NAME\}\}/$repl_ref->[1]/;
	$cmd =~ s/\{\{OLD\}\}/$repl_ref->[2]/;
	$cmd =~ s/\{\{NEW\}\}/$repl_ref->[3]/;
	
	$status = system $cmd;
	if ($status) {
		die "Could not replace $repl_ref->[1] in $java_file.\n"
		  . "Command failed: $cmd\n";
	}
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
	my $cmd      = "'$java_home/javac' -source 8 -target 8 -sourcepath '$src_path' -d '$tmp_path' '$java_file'";
	$status      = system $cmd;
	if ($status) {
		die "Command failed: $cmd\n";
	}
	
	# delete old jar file
	my $jar_path = $project_path . '/midica.jar';
	if (-e $jar_path) {
		unlink $jar_path or die "Delete of '$jar_path' failed: $!\n";
	}
	
	# create new jar file
	my $manifest_path = $project_path . '/build_helper/manifest';
	$cmd              = "jar -cvfm '$jar_path' '$manifest_path' "
	                  . " -C '$tmp_path' org"
	                  . " -C '$tmp_path' com";
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
	my $cmd = "'$java_home/javadoc' -docletpath '$doclet_path'"
	        . " -doclet $doclet_name"
	        . " -d '$javadoc_path'"
	        . " -sourcepath '$source_path'"
	        . " -subpackages org.midica:com.sun.kh"
	        . " -overview '$overview_path'"
	        . " -private"
	        . " -encoding utf-8"
	        . " -author"
	        . " -charset utf-8"
	        . " -keywords"
	        . " -notimestamp"
	        . " -windowtitle 'Midica $version - Javadoc'"
	        . " -doctitle 'Midica - the MIDI Processing and Programming Tool'"
	        . " -header '<a href=\"https://www.midica.org/\" target=\"_top\"><b>Midica</b></a><br>$version'"
	;
	my $status = system $cmd;
	if ($status) {
		die "Could not create javadoc.\n"
		  . "Command failed: $cmd\n";
	}
}

sleep 1;

exit 0;
