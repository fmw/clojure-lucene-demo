(defproject clojure-lucene-demo "1.0.0-SNAPSHOT"
  :description "Demo implementation of Lucene code in Clojure"
  :license {:name "Apache License, version 2."}
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [clojure-couchdb "0.4.5"]
                 [org.apache.lucene/lucene-core "3.0.3"]
                 [org.apache.lucene/lucene-queries "3.0.3"]]
  :dev-dependencies [[org.clojars.autre/lein-vimclojure "1.0.0"]]
  :main clojure-lucene-demo.core)

