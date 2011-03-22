; src/clojure_lucene_demo/core.clj: Demonstrating Lucene API using Clojure
;
; Copyright 2011, F.M. (Filip) de Waard <fmw@vix.io>.
;
; Licensed under the Apache License, Version 2.0 (the "License");
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
; 
; http://www.apache.org/licenses/LICENSE-2.0
; 
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.

(ns clojure-lucene-demo.core
  (:use [clojure.string :only (lower-case)])
  (:import (org.apache.lucene.document
       Document Field Field$Store Field$Index NumericField)
     (org.apache.lucene.analysis.standard StandardAnalyzer)
     (org.apache.lucene.store NIOFSDirectory RAMDirectory)
     (org.apache.lucene.search
       IndexSearcher QueryWrapperFilter TermQuery Sort)
     (org.apache.lucene.queryParser QueryParser)
     (org.apache.lucene.index IndexWriter IndexWriter$MaxFieldLength
                              IndexReader Term)
     (org.apache.lucene.util Version)
     (java.io File)))

(defn create-analyzer []
  (StandardAnalyzer. (. Version LUCENE_30)))

(defn create-directory [path]
  (if (= path :RAM)
    (RAMDirectory.)
    (NIOFSDirectory. (File. path))))

(defn create-index-reader [#^Directory directory]
  (. IndexReader open directory))

(defn #^Field create-field
  "Creates a new Lucene Field object."
  ([field-name value]
   (create-field field-name value :stored :analyzed))
  ([field-name value & options]
    (Field. field-name (str value)
      (if (some #{:stored} options)
        (Field$Store/YES)
        (Field$Store/NO))
      (if (some #{:analyzed} options)
        (Field$Index/ANALYZED)
        (if (some #{:dont-index} options)
          (Field$Index/NO)
          (Field$Index/NOT_ANALYZED))))))

(defn create-document
  "Creates a new Lucene Document object using the input provided."
  [item]
  (let [{:keys [title description category]} item
      #^Document document (Document.)]

    (doto document
      ; index a fulltext field with all the values to search on
      ; mashed together in a single value, but there is no need
      ; to store this field.
      (.add (create-field "fulltext"
                (apply str(interpose " " [title description])) :analyzed))

      ; this field is included to run filters on. It is indexed, 
      ; but not analyzed or tokenized (i.e. you need to use
      ; literal values in the filter or it won't match).
      (.add (create-field "category" category :stored))

      ; these fields are just stored in order to be able to display
      ; them in the search results without loading the document
      ; from a database
      (.add (create-field "title" title :stored :dont-index))
      (.add (create-field "description" description :stored :dont-index)))))

(defn write-index! [directory items]
  (let [analyzer (create-analyzer)
        writer (IndexWriter.
                 directory analyzer IndexWriter$MaxFieldLength/UNLIMITED)]

    (doto writer
      (.setRAMBufferSizeMB 64)) ;maybe .setUseCompoundFile false?

    (doseq [item items]
      (.addDocument writer (create-document item)))

    (doto writer
      (.optimize)
      (.close))))

(defn get-doc [reader doc-id]
  (.document reader doc-id))

(defn get-docs [reader docs]
  (map #(get-doc reader (.doc %)) docs))

(defn create-filter [filters]
  "Creates a filter for the category, which is wrapped in double quotes."
  (let [{:keys [category]} filters]

    ; I want to use a BooleanQuery here, wrapping the category
    ; as well as some NumericRangeQuery objects on fields that
    ; aren't in the dummy data set, but lets start simple for
    ; this little proof-of-concept.

    ; Originally, I used a normal filter (wrapped in a ChainedFilter)
    ; but due to the fact that ChainedFilter isn't in lucene-core
    ; I decided to go for a QueryWrapperFilter instead.

    (when category
      (QueryWrapperFilter.
        (TermQuery. (Term. "category" (lower-case category)))))))

(defn search [query query-filter limit reader analyzer]
  (let [searcher (IndexSearcher. reader)
        parser (QueryParser. (. Version LUCENE_30) "fulltext" analyzer)
        q (.parse parser query)
        top-docs (if-not (nil? query-filter)
                   (.search searcher q query-filter limit (new Sort))
                   (.search searcher q limit))]

    (. searcher close)

    {:total-hits (.totalHits top-docs)
     :docs (.scoreDocs top-docs)}))

