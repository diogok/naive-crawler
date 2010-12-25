# Naive Crawler 

A very naive crawler written a while ago in clojure, it takes an initial url and save this page content and every link found on it of the same domain recursive style and saves it to disk, until there is nothing left... not even memory ;)

For real web crawler take a look at [Bixo](http://github.com/bixo/bixo/), that uses java and hadoop, or if you intent to build your own a nice option is to use [clj-sys/works](https://github.com/clj-sys/work) and maybe [neo4j](http://neo4j.org).
