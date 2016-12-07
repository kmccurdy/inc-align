# inc-align

A small Clojure application which extends the [Berkeley Aligner](https://code.google.com/archive/p/berkeleyaligner/) to support **incremental alignment**, e.g. using an already-trained alignment model to generate alignments on new data *without* having to retrain the model.

## Installation

The easiest option is to [download the jar file](https://github.com/kmccurdy/inc-align/blob/master/target/uberjar/inc-align-0.1.0-SNAPSHOT-standalone.jar) and run it from the command line (see below).

Code-tweakers can also clone this repo and use the [Leningen](https://github.com/technomancy/leiningen) toolbox (e.g. `lein run`) to try out variations. Note that you may hit dependency issues - I installed the Berkeley Aligner jar in my local lib as per [these instructions](https://www.reddit.com/r/Clojure/comments/35484f/adding_java_jar_files_to_leiningen_and_then/) to get it in the classpath.

## Usage

### Requirements

To use inc-align, you should have:

- A Berkeley Aligner model which has already been trained on the relevant language combination with the following configuration settings in the `*.conf` file:
    - `forwardModels  MODEL1 HMM` (inc-align supports HMM, but not Model 2)
    - `reverseModels MODEL1 HMM`
    - `mode  JOINT JOINT` (inc-align assumes jointly trained models)
    - `saveParams true`
    - `alignTraining`
- Data to be aligned, formatted as required by the trained model (i.e. sentence-aligned, tokenized, etc... check the Berkeley Aligner documentation)

This has only been tested with the unsupervised version of the Aligner, but it probably works for the supervised version as well. Let me know if you try!

### Usage

Run the jar with the following arguments:

```
  -p, --params PARAMS     REQUIRED: Directory with trained Berkeley Aligner
  -d, --data DATA         REQUIRED: Directory with data to align
  -f, --l2 L2          f  Suffix for L2 language files
  -e, --l1 L1          e  Suffix for L1 language files
  -h, --help
```
Like so:

    $ java -jar inc-align-0.1.0-standalone.jar -p model-output-dir -d sentences-to-align-dir [-e l1_suffix -f l2_suffix]

In passing directory paths to the -p and -d arguments, note that tilde expansion isn't supported, but relative paths (e.g. `./`, `../../`) are.

### Output

The directory `inc-align-output` will be created in the data directory, containing three files: `output.{l1 suffix}` and `output.{l2 suffix}` with all of the aligned sentences, and `output.align` with alignments in Pharoah notation.

## Issues

One currently known issue: loading parameters from a model output directory which has been *decompressed* - e.g. expanded from a tarball - throws a `java.io.OptionalDataException`. This means inc-align can be used with models trained locally, i.e. on the same machine, but makes using models trained elsewhere difficult. Anyone with thoughts on the root of the problem and/or how to fix it: I'd be happy to hear them. 

## Extras

There are two standalone helper scripts:

- [tknze](https://github.com/kmccurdy/inc-align/blob/master/src/inc_align/tknze.clj) takes a text file (ideally already sentence-aligned => one sentence per line) and tokenizes it in the style of the BA example data
- [vis](https://github.com/kmccurdy/inc-align/blob/master/src/inc_align/vis.clj) takes a model and two aligned data files  (e.g. `data.e` and `data.f`), and writes a new file `data.vis` with their alignments visualized as distortion matrices

See comments in the files for usage.

## License

Copyright © 2016 Kate McCurdy

Distributed under the GNU GPL v2. 

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
