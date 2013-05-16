competition
===========

Source code for "Non-local interaction via diffusible resource prevents 
coexistence of cooperators and cheaters in a lattice model" (Borenstein,
et al., PLOS ONE 2013). http://dx.plos.org/10.1371/journal.pone.0063304

Copyright (c) 2013, David Bruce Borenstein.

This work is licensed under the Creative Commons 2.0 BY-NC license.

Attribute (BY) -- You must attribute the work in the manner specified 
by the author or licensor (but not in any way that suggests that they 
endorse you or your use of the work).

Noncommercial (NC) -- You may not use this work for commercial purposes.

For the full license, please visit:
http://creativecommons.org/licenses/by-nc/3.0/legalcode

======================
A NOTE ABOUT THIS CODE
======================

The source code in this archive is involved in several projects. As such,
it includes many capabilities unrelated to this paper. The documentation
below explains how to use features related to the Borenstein, et al. 2013
PLOS ONE paper only.

===================
INSTALLING THE CODE
===================

This program is implemented in Java 6.0. It depends on 
matrix-toolkit-java (MTJ), which is included in the code. MTJ is
distributed under the lesser GPL license.

Building the program
--------------------

The easiest way to use the program is to compile it and run it directly.
To do this, it is first necessary to build the project. To build the
program, you will need a copy of Apache Ant (http://ant.apache.org/).
Once you have ant installed, build the project by writing

  ant clean build jar

To run the resulting jar file, see "RUNNING THE CODE" below.

Editing the project
-------------------

The working directory includes an Eclipse project file, meaning
that the source code can be loaded and compiled from the Eclipse
IDE. Follow the directions for your version of Eclipse for importing
an existing project.

================
RUNNING THE CODE
================

The .jar file is built in the build/ directory. To invoke it, call

    java -jar ./build/efficient.jar param1=value param2=value...

The simulation requires numerous parameters that are described below. You
need to set all of them.

MAX_R=float -- sets scale for solute integration. Typical value is 10E-5.
   Integrator will fail to converge above R=0.25. Lower values exhibit low
   error, but have longer startup times.

RANDOM_SEED=[*|long] -- If set to *, the system time will be used to generate
   a random number seed. If a long integer is supplied, this will be used
   instead. Included for reproducibility.

PRODUCTION=float -- How many units of resource output by each producer. 
  Corresponds to alpha in eq. 3. 

GROWTH=float -- basal growth rate. Corresponds to g_0 in eqs 1. and 2.

DECAY=float -- decay rate for solute. Corresponds to beta in eq. 3.

OUTPUT=[FULL|REDUCED|SPARSE|MINIMAL] -- specifies how much detail to include in
   the simulation results. Only FULL includes visualizations.

IC_ARGUMENT -- the meaning of this argument depends on the initial condition.
   See the source file for the initial condition of interest for more information.
   Initial condition source files are in src/structures/cell/ic. 

IC=string -- specifies the name of the initial condition to use. The ones related
   to the paper are WellMixed, TwoDomains, SingleCheater and SingleProducer.

dx=1.0 -- always set this to 1.0. While this parameter specifies the length scale for
   certain calculations, others assume implicitly that the value is 1.0. As a result,
   values other than 1.0 will result in unexpected behavior.

RANDOMIZE_CHEATERS=[TRUE|FALSE]-- unrelated to this project. Ignore.

RANDOMIZE_COOPERATORS=[TRUE|FALSE] -- unrelated to this project. Ignore.

THRESHOLD=float -- Unrelated to this project. Ignore.

CELL_OPERATOR=ContinuousReplacement -- For the purpose of this paper, set this
   parameter to ContinuousReplacement.

MAX_TIME_STEP=integer -- Specifies the maximum number of cell divisions to allow
   before halting the current instance. The instance will also end if one cell type
   reaches fixation.

HALT_COUNT=integer -- No effect if set to -1. If set to a value greater than one,
   the program will halt when exactly that many cheaters exist. Used to calculate
   the radial distribution function at a specified cheater count.

REPLICATES=integer -- specifies the number of times to run the simulation. OUTPUT
   determines how much information is captured from each replicate.

DIFFUSION=float -- specifies the diffusion constant. Corresponds to D in equation 3.

BENEFIT=float -- fitness benefit per unit of solute. Corresponds to gamma in eqs
   1 and 2.

H=integer -- System height, in cells.

W=integer -- System width, in cells.

PATH -- Where to output the results.
