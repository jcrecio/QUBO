# Quadratic Unconstrained Binary Optimization (QUBO)
This project focuses in optimizing QUBO using an evolutionary algorithm.

For QUBO, given a matrix of real numbers $Q ∈ \mathbb{R}^{n x n}$, the problem consists of maximizing the expression
$\sum_{i=1}^{n} \sum_{j=1}^{n} Q_{ij}x_ix_j$ where $x$ is a vector of binary values, and $x_i$ represents the i-th binary value of the vector. This problem is of great theoretical and practical interest because it can be shown that any compressible binary optimization problem can be reduced to an instance of QUBO (Quadratic Unconstrained Binary Optimization), and modern Quantum Annealers (a type of quantum computer), such as those from D-WAVE and Fujitsu, are capable of efficiently solving these problems.

The skeleton code of the project is branched from a simple evolutionary algorithm available at https://github.com/NEO-Research-Group/SimpleEvolutionaryAlgorithm.git in Java. This project has been created in the University of Málaga by Francisco Chicano.

The current repository includes the implementation of the QUBO evaluation function and performs an empirical study on the influence of two parameters of the evolutionary algorithm on the optimization problem's outcome.

 The following parameters can be used: population size, mutation probability, type of replacement operators (selection, crossover, or mutation - additional operators need to be implemented), or the type of population initialization. If you wish to choose other parameters, consult with the professor beforehand.


The parameters are studied together: first one parameter will be studied, and once the best value is determined, the other parameter will be studied using the previous obtained value from the first parameter

The number of objective function evaluations is not less than 1000, and at least 10 instances of the QUBO problem are used for a fixed size greater than 100 binary variables. Each execution is repeated 30 times with different random seeds, and the mean and standard deviation of the best solution found for each instance is reported.

Some references on QUBO:

Oylum Şeker, Neda Tanoumand, Merve Bodur (2020), "Digital Annealer for quadratic unconstrained binary optimization: a comparative performance analysis" (https://arxiv.org/abs/2012.12264)

Gian Giacomo Guerreschi (2021), "Solving Quadratic Unconstrained Binary Optimization with divide-and-conquer and quantum algorithms" (https://arxiv.org/abs/2101.07813)
