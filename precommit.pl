#!/usr/bin/perl -w

# This Source Code Form is subject to the terms of the
# Mozilla Public License, v. 2.0. 
# If a copy of the MPL was not distributed with this file,
# You can obtain one at http://mozilla.org/MPL/2.0/.

###############################################################################
# This is supposed to be called from the actual precommit hook for Midica.
# It calculates a new minor version from the current time and writes it into
# the according variable in Midica.java.
# 
# Start it from the real precommit hook (.git/hooks/pre_commit)
# with the following two lines (assuming it's a bourne shell script):
# 
# project_path=/path/to/project/midica
# exec perl -w -CSD -Mutf8 $project_path/precommit.pl $project_path
# 
#
# If you don't want to start it as the last command, you have to evaluate
# the exit status by .git/hooks/pre_commit yourself.
###############################################################################

use strict;

# get project path from command line
my $project_path = $ARGV[ 0 ];

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
if ( ! $is_commit ) {
	print STDERR "No files staged. Nothing to commit.\n";
	exit 1;
}

# check if Midica.java is clean (either fully staged or not modified at all).
my $java_file = $project_path . '/src/org/midica/Midica.java';
my $is_clean  = 1;
$output = `git status -z --porcelain '$java_file'`;
if ($output) {
	my $modified_flag = substr $output, 1, 1;
	
	# modified?
	if ( 'M' eq $modified_flag ) {
		$is_clean = 0;
	}
}
if ( ! $is_clean ) {
	print STDERR "Midica.java is modified but not fully staged.\n";
	exit 1;
}

# Build up the command to replace VERSION_MINOR in Midica.java.
# 1. perl and options and opening single quote (') for the regex
# 2. the regex
# 3. closing single quote for the regex and single-quoted file parameter
my $cmd = "perl -CSD -Mutf8 -p -i -e '"
        . 's/(\bint\s+VERSION_MINOR\s*=)\s*(\d+)/\1 ' . time() . '/'
        . "' '$java_file'";

# execute the replacement command
qx { $cmd };

# add Midica.java to the staging area
`git add '$java_file'`;

sleep 1;

exit 0;
