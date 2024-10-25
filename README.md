# ns-graph (beta)

Very rough around the edges, but this tool gets the job done quote nicely. Since it's launched as a 
[Clojure Tool](https://github.com/clojure/tools.tools?tab=readme-ov-file), it doesn't use the classpath of the project that it's rendering.


### Installation

```sh
clj -Ttools install io.github.mortenschioler/ns-graph '{:git/sha "8aab5ea34f38b91b96fcfacfe95bf8e2d76d62c0"}' :as ns-graph
```

### Usage

```sh
clj -T:ns-graph export '{:prefix "your.project" :exclude #{your.project.logging}}' #example parameters
xdg-open ns-graph.pdf
```

