#!/usr/bin/php
<?php
/**
 * This is a specialized version of this script which throws away anything from before 1st January 2007.
 *
 * This function takes one input file and two output files as arguments and filters the input into non-tvmeter
 * channels (radio + dk4 + tv2news) and everything else
 * args:
 * -i <file>
 * -tvm <file>
 * -nontvm <file>
 */

$args=arguments($argv);
$input=read_param("input", $args, null);
$tvm=read_param("tvm", $args, null);
$nontvm=read_param("nontvm", $args, null);
$debug=read_param("debug", $args, null);
$limit=read_param("limit", $args, null);

if ($input==null || $tvm==null || $nontvm==null) {
    usage();
}

if ($debug) {
    echo 'Splitting with arguments: '.$input.' '.$tvm.' '.$nontvm."\n";
}

if (!file_exists($input)) {
    echo "No such file: '".$input."'\n";
    exit(1);
}

$shard_file_pairs=file($input);
$count=0;
$tvm_handle = fopen($tvm, "w");
$nontvm_handle = fopen($nontvm, "w");
$ignored_handle = fopen("ignored.txt", "w");
foreach ($shard_file_pairs as $shard_file_pair) {
    $count++;
    $pair_array=explode(",", $shard_file_pair);
    $shard_url=trim($pair_array[0]);
    $file_url=trim($pair_array[1]);
    if ($debug) {
        echo "'$shard_url' '$file_url'\n";
    }
    $file_path = parse_url($file_url, PHP_URL_PATH);
    if ($debug) {
        echo "File path: '$file_path'\n";
    }
    $file_pathinfo = pathinfo($file_path);
    if ($debug) {
        print_r($file_pathinfo);
    }
    /**
     * Here we have files like
     *  [extension] => mpeg | wav
     *  [filename] => dr1_196.250_K11-DR1_mpeg2_20080623055601_20080624012201_encoder2-2
     *
     * and
     *
     * [extension] => ts
     * [filename] => mux2.1279735200-2010-07-21-20.00.00_1279738800-2010-07-21-21.00.00_dvb1-1
     */

    $ts_regexp="/(mux([0-9]*)).(.*)-(\\d{4})-.*\\d{4}-.*/" ;  //year is preg_match()[4]
    $bart_regexp="/(.*)_(.*)_(\\d{4})\\d{10}_.*/";    //year is preg_match()[3]
    if ($file_pathinfo['extension']=="ts") {
        preg_match($ts_regexp, $file_pathinfo['filename'], $match);
        $year = $match[4];
    } else {
        preg_match($bart_regexp, $file_pathinfo['filename'], $match);
        $year = $match[3];
    }
    if ($debug) {
        print_r($match);
        echo "Year: $year\n";
    }
    $is_non_tvm = $file_pathinfo['extension']=="wav" || strpos($file_pathinfo['basename'], "dk4", 0) === 0;
    if ($debug) {
        if ($is_non_tvm) {
            echo 'Non-tvm:';
        } else {
            echo '    tvm:';
        }
        echo $file_pathinfo['basename']."\n";
    }
    if ($year > 2006) {
        if ($is_non_tvm) {
            $write_handle = $nontvm_handle;
            fwrite($nontvm_handle, $shard_url."\n");
        } else {
            $write_handle = $tvm_handle;
            fwrite($tvm_handle, $shard_url."\n");
        }
        if ($debug) {
            fwrite($write_handle, $file_url."\n");
        }
    } else {
        fwrite($ignored_handle, $shard_file_pair."\n");
        if ($debug) {
            echo "Ignoring\n";
        }
    }

    if ( ($limit!=null) && ($count==$limit-1) ) {
        echo 'Finished (debug only)'."\n";
        exit(0);
    }
}
fclose($tvm_handle);
fclose($nontvm_handle);



function read_param($param, $args, $default_value) {
	if (array_key_exists($param, $args)) {
		return $args[$param];
	} else {
		return $default_value;
	}
}

function arguments($argv) {
	$_ARG = array();
	foreach ($argv as $arg) {
		if (ereg('--[a-zA-Z0-9]*=.*',$arg)) {
			$str = split("=",$arg); $arg = '';
			$key = ereg_replace("--",'',$str[0]);
			for ( $i = 1; $i < count($str); $i++ ) {
				$arg .= $str[$i];
			}
                        $_ARG[$key] = $arg;
		} elseif(ereg('-[a-zA-Z0-9]',$arg)) {
			$arg = ereg_replace("-",'',$arg);
			$_ARG[$arg] = 'true';
		}

	}
	return $_ARG;
}

function usage() {
    echo "Usage: php filter_tvmeter.php --input=<file> --tvm=<file> --nontvm=<file> [--debug] [--limit=<int>]\n";
    exit(1);
}

?>
