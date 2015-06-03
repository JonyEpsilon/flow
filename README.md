# Flow project code and data

This repository contains code and data to replicate the results in the paper "An algorithm for discovering Lagrangians automatically from data" by D.J.A. Hills, A.M. Grütter, and J.J. Hudson (JonyEpsilon).

## Restricted polynomial search

The code is written in Mathematica and can be found in the `nb` sub-directory. To run it, first open `ELSolver.nb` and run all of the cells therein. Then look at `EL examples.nb` and work through the examples presented in the paper.

## General expression search (symbolic regression)

The code is written in Clojure. Follow the getting started instructions for Gorilla REPL http://gorilla-repl.org/ . Once you have Gorilla REPL installed, then execute `lein gorilla` from the project directory, open the worksheet `ws\pendulum.clj` and run the code therein.

## Data

The data used for the experiments is generated in `EL examples.nb` and can be found in the `data` subdirectory.

## License

The code is licensed to you under the MIT license. See the LICENSE file for details.
