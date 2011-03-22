; test/clojure_lucene_demo/test/core.clj: Tests for Lucene code.
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

(ns clojure-lucene-demo.test.core
  (:use [clojure-lucene-demo.core] :reload)
  (:use [clojure.test])
  (:use [clojure.contrib.reflect :only (get-field)])
  (:import (org.apache.lucene.search ScoreDoc)))

(def dummy-docs
    [{:title "Hacker News" 
     :description "A community-driven news and discussion site."
     :category "technology"}
    {:title "Planet Clojure"
     :description "Aggregates Clojure-related weblog posts."
     :category "clojure"}
    {:title "Planet Python"
     :description "Aggregates Python-related weblog posts."
     :category "python"}
    {:title "Planet Java.org"
     :description "Aggregates Java-related weblog posts."
     :category "java"}
    {:title "NOS.nl"
     :description "News from the Dutch national broadcaster."
     :category "general"}])

(deftest test-create-analyzer
    (testing "test if Lucene analyzers are created correctly."
      (is (= (class (create-analyzer))
             org.apache.lucene.analysis.standard.StandardAnalyzer))))

(deftest test-create-directory
    (testing "test if Lucene directories are created correctly."
      (is (= (class (create-directory :RAM))
             org.apache.lucene.store.RAMDirectory))
      (let [directory (create-directory "/tmp/test")]
        (is (= (class directory) org.apache.lucene.store.NIOFSDirectory))
        (is (or 
              (= (str (.getFile directory)) "/tmp/test")
              (= (str (.getFile directory)) "/private/tmp/test"))))))

(deftest test-create-index-reader
    (testing "test if Lucene IndexReaders are created correctly."
      (let [dir (create-directory :RAM)]
        ; write to index to avoid no segments file error
        (do (write-index! dir dummy-docs))
        (is (= (class (create-index-reader dir)) 
               org.apache.lucene.index.ReadOnlyDirectoryReader)))))

(deftest test-create-field
    (testing "test if create-field properly creates a Lucene Field"
      (let [field (create-field "foo" "bar")]
        (is (= (.name field) "foo"))
        (is (= (.stringValue field) "bar"))
        (is (.isIndexed field))
        (is (.isStored field))
        (is (.isTokenized field)))
      
      (let [field (create-field "foo" "bar" :stored :analyzed)]
        (is (= (.name field) "foo"))
        (is (= (.stringValue field) "bar"))
        (is (.isIndexed field))
        (is (.isStored field))
        (is (.isTokenized field)))
      
      (let [field (create-field "foo" "bar" :stored)]
        (is (= (.name field) "foo"))
        (is (= (.stringValue field) "bar"))
        (is (.isIndexed field))
        (is (.isStored field))
        (is (not (.isTokenized field))))

      (let [field (create-field "foo" "bar" :stored :dont-index)]
        (is (= (.name field) "foo"))
        (is (= (.stringValue field) "bar"))
        (is (not (.isIndexed field)))
        (is (.isStored field))
        (is (not (.isTokenized field))))

      
      (let [field (create-field "foo" "bar" :analyzed)]
        (is (= (.name field) "foo"))
        (is (= (.stringValue field) "bar"))
        (is (.isIndexed field))
        (is (not (.isStored field)))
        (is (.isTokenized field)))))

(deftest test-create-document
  (testing "Check if a document is correctly tranlated to a Lucene doc"
    (let [document (create-document (first dummy-docs))]

      (is (= (class document) org.apache.lucene.document.Document))

      (let [field (.getField document "title")] 
        (is (= (.name field) "title"))
        (is (= (.stringValue field) "Hacker News"))
        (is (.isStored field))
        (is (not (.isIndexed field)))
        (is (not (.isTokenized field))))
      
      (let [field (.getField document "description")] 
        (is (= (.name field) "description"))
        (is (= (.stringValue field)
               "A community-driven news and discussion site."))
        (is (.isStored field))
        (is (not (.isIndexed field)))
        (is (not (.isTokenized field))))

      (let [field (.getField document "category")] 
        (is (= (.name field) "category"))
        (is (= (.stringValue field) "technology"))
        (is (.isStored field))
        (is (.isIndexed field))
        (is (not (.isTokenized field)))))

    (let [document (create-document (nth dummy-docs 1))]

      (is (= (class document) org.apache.lucene.document.Document))

      (let [field (.getField document "title")] 
        (is (= (.name field) "title"))
        (is (= (.stringValue field) "Planet Clojure"))
        (is (.isStored field))
        (is (not (.isIndexed field)))
        (is (not (.isTokenized field))))
      
      (let [field (.getField document "description")] 
        (is (= (.name field) "description"))
        (is (= (.stringValue field)
               "Aggregates Clojure-related weblog posts."))
        (is (.isStored field))
        (is (not (.isIndexed field)))
        (is (not (.isTokenized field))))

      (let [field (.getField document "category")] 
        (is (= (.name field) "category"))
        (is (= (.stringValue field) "clojure"))
        (is (.isStored field))
        (is (.isIndexed field))
        (is (not (.isTokenized field)))))
      
    (let [document (create-document (nth dummy-docs 2))]

      (is (= (class document) org.apache.lucene.document.Document))

      (let [field (.getField document "title")] 
        (is (= (.name field) "title"))
        (is (= (.stringValue field) "Planet Python"))
        (is (.isStored field))
        (is (not (.isIndexed field)))
        (is (not (.isTokenized field))))
      
      (let [field (.getField document "description")] 
        (is (= (.name field) "description"))
        (is (= (.stringValue field)
               "Aggregates Python-related weblog posts."))
        (is (.isStored field))
        (is (not (.isIndexed field)))
        (is (not (.isTokenized field))))

      (let [field (.getField document "category")] 
        (is (= (.name field) "category"))
        (is (= (.stringValue field) "python"))
        (is (.isStored field))
        (is (.isIndexed field))
        (is (not (.isTokenized field)))))
  
    (let [document (create-document (nth dummy-docs 3))]

      (is (= (class document) org.apache.lucene.document.Document))

      (let [field (.getField document "title")] 
        (is (= (.name field) "title"))
        (is (= (.stringValue field) "Planet Java.org"))
        (is (.isStored field))
        (is (not (.isIndexed field)))
        (is (not (.isTokenized field))))
      
      (let [field (.getField document "description")] 
        (is (= (.name field) "description"))
        (is (= (.stringValue field)
               "Aggregates Java-related weblog posts."))
        (is (.isStored field))
        (is (not (.isIndexed field)))
        (is (not (.isTokenized field))))

      (let [field (.getField document "category")] 
        (is (= (.name field) "category"))
        (is (= (.stringValue field) "java"))
        (is (.isStored field))
        (is (.isIndexed field))
        (is (not (.isTokenized field)))))

    (let [document (create-document (nth dummy-docs 4))]
      (is (= (class document) org.apache.lucene.document.Document))

      (let [field (.getField document "title")] 
        (is (= (.name field) "title"))
        (is (= (.stringValue field) "NOS.nl"))
        (is (.isStored field))
        (is (not (.isIndexed field)))
        (is (not (.isTokenized field))))
      
      (let [field (.getField document "description")] 
        (is (= (.name field) "description"))
        (is (= (.stringValue field)
               "News from the Dutch national broadcaster."))
        (is (.isStored field))
        (is (not (.isIndexed field)))
        (is (not (.isTokenized field))))

      (let [field (.getField document "category")] 
        (is (= (.name field) "category"))
        (is (= (.stringValue field) "general"))
        (is (.isStored field))
        (is (.isIndexed field))
        (is (not (.isTokenized field)))))))

(deftest test-write-index!
  (testing "test indexing process."
    ; not really testing anything here (yet), as this functionality
    ; is also tested in a lot of other tests (e.g. test-index-reader)
    (write-index! (create-directory :RAM) dummy-docs)))

(deftest test-get-doc-and-get-docs
    (testing "test document retrieval from reader."
      (let [dir (create-directory :RAM)]
        (do (write-index! dir dummy-docs))
        (let [reader (create-index-reader dir)
              first-doc (get-doc reader 0)
              other-docs (get-docs reader [(ScoreDoc. 1 1.0)
                                     (ScoreDoc. 2 1.0)
                                     (ScoreDoc. 3 1.0)
                                     (ScoreDoc. 4 1.0)])]

          (is (= (.get first-doc "title") "Hacker News"))
          (is (= (.get first-doc "description")
                       "A community-driven news and discussion site."))
          (is (= (.get first-doc "category") "technology"))

          (is (= (.get (first other-docs) "title") "Planet Clojure"))
          (is (= (.get (first other-docs) "description")
                       "Aggregates Clojure-related weblog posts."))
          (is (= (.get (first other-docs) "category") "clojure"))
          
          (is (= (.get (nth other-docs 1) "title") "Planet Python"))
          (is (= (.get (nth other-docs 1) "description")
                       "Aggregates Python-related weblog posts."))
          (is (= (.get (nth other-docs 1) "category") "python"))
          
          (is (= (.get (nth other-docs 2) "title") "Planet Java.org"))
          (is (= (.get (nth other-docs 2) "description")
                       "Aggregates Java-related weblog posts."))
          (is (= (.get (nth other-docs 2) "category") "java"))
          
          (is (= (.get (nth other-docs 3) "title") "NOS.nl"))
          (is (= (.get (nth other-docs 3) "description")
                       "News from the Dutch national broadcaster."))
          (is (= (.get (nth other-docs 3) "category") "general"))))))

(deftest test-create-filter
    (testing "test if filters are constructed properly."
      (is (nil? (create-filter {})))
      (let [flt (create-filter {:category "technology"})
            ; get the corresponding Term object from the
            ; QueryWrapperFilter's private "query" field
            term (.getTerm (get-field
                             org.apache.lucene.search.QueryWrapperFilter
                             "query"
                             flt))]

        (is (= (class flt) org.apache.lucene.search.QueryWrapperFilter))
        (is (= (.field term) "category"))
        (is (= (.text term) "technology")))
      
      ; check if filters are correctly converted to lowercase
      (let [flt (create-filter {:category "TecHnOlOgY"})
            term (.getTerm (get-field
                             org.apache.lucene.search.QueryWrapperFilter
                             "query"
                             flt))]

        (is (= (class flt) org.apache.lucene.search.QueryWrapperFilter))
        (is (= (.field term) "category"))
        (is (= (.text term) "technology")))))

(deftest test-search
    (testing "testing search."
      (let [dir (create-directory :RAM)]

        (do (write-index! dir dummy-docs))
        (let [reader (create-index-reader dir)
              analyzer (create-analyzer)]

          (let [result (search "planet" nil 5 reader analyzer)
                docs (get-docs reader (:docs result))]
            (is (= (:total-hits result) 3))
          
          (is (= (.get (first docs) "title") "Planet Clojure"))
          (is (= (.get (first docs) "description")
                       "Aggregates Clojure-related weblog posts."))
          (is (= (.get (first docs) "category") "clojure"))
          
          (is (= (.get (nth docs 1) "title") "Planet Python"))
          (is (= (.get (nth docs 1) "description")
                       "Aggregates Python-related weblog posts."))
          (is (= (.get (nth docs 1) "category") "python"))
          
          (is (= (.get (last docs) "title") "Planet Java.org"))
          (is (= (.get (last docs) "description")
                       "Aggregates Java-related weblog posts."))
          (is (= (.get (last docs) "category") "java"))

          (let [flt (create-filter {:category "clojure"})
                result (search "planet" flt 5 reader analyzer)
                document (first (get-docs reader (:docs result)))
                term (.getTerm (get-field
                             org.apache.lucene.search.QueryWrapperFilter
                             "query"
                             flt))]
            ; there is only one document with "clojure" as the value of
            ; the category field, so I expect to get a single result:
            (is (= (:total-hits result) 1))
            
            (is (= (.get document "title") "Planet Clojure"))
            (is (= (.get document "description")
                         "Aggregates Clojure-related weblog posts."))
            (is (= (.get document "category") "clojure"))
            
            ; this is redundant with the test for create-filter, but
            ; it never hurts to check again.
            (is (= (class flt) org.apache.lucene.search.QueryWrapperFilter))
            (is (= (.field term) "category"))
            (is (= (.text term) "clojure")))
            
          ; filters are case-sensitive, but create-filter
          ; function uses lower-case on the value, so this
          ; should work:
          (let [flt (create-filter {:category "Clojure"})
                result (search "planet" flt 5 reader analyzer)]
            (is (= (:total-hits result) 1))))))))
