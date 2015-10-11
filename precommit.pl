#!/usr/bin/perl -w

# This Source Code Form is subject to the terms of the
# Mozilla Public License, v. 2.0. 
# If a copy of the MPL was not distributed with this file,
# You can obtain one at http://mozilla.org/MPL/2.0/.


# This file is not yet complete. Do not yet use it.
print STDERR "Don't use this script yet\n";
exit 0;

###############################################################################
# This is supposed to be called from the actual precommit hook for Midica.
# It calculates a new minor version from the current time and writes it into
# the according variable in Midica.java.
# 
# Start it from the real precommit hook (.git/hooks/pre_commit)
# with the following two lines (assuming it's a bourne shell script):
# 
# export project_path=/path/to/project/midica
# exec perl -w -CSD -Mutf8 $project_path/precommit.pl $project_path
# 
#
# If you don't want to start it as the last command, you have to evaluate
# the exit status by .git/hooks/pre_commit yourself.
###############################################################################

my $project_path = $ARGV[ 0 ];
my $java_file    = $project_path . '/src/org/midica/Midica.java';
my $timestamp    = time();

# TODO: check if anything is going to be committed at all.


# TODO: check if Midica.java is clean (either fully staged or not modified at all).

# build up the replacement command
# 1. perl and options and opening single quote (') for the regex
# 2. the regex
# 3. closing single quote for the regex and single-quoted file parameter
my $cmd = "perl -CSD -Mutf8 -p -i -e '"
        . 's/(\bint\s+VERSION_MINOR\s*=)\s*(\d+)/\1 ' . $timestamp . '/'
        . "' '$java_file'";

# execute the replacement command
qx { $cmd };

# TODO: add Midica.java to the staging area

# TODO: delete
print STDOUT "S T O P !\n";
exit 1;

exit 0;
