(ns clojure-lucene-demo.core
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
   (create-field field-name value true true))
  ([field-name value analyzed]
   (create-field field-name value true analyzed))
  ([field-name value stored analyzed]
    (Field. field-name (str value)
      (if stored
        (Field$Store/YES)
        (Field$Store/NO))
      (if analyzed
         (Field$Index/ANALYZED)
         (Field$Index/NO)))))

(defn create-document
  "Creates a new Lucene Document object using the input provided."
  [item]
  (let [{:keys [title description category]} item
      #^Document document (Document.)]

    (doto document
      ; create a fulltext field with all the values to search on
      ; mashed together in a single value
      (.add (create-field "fulltext"
                (apply str(interpose " " [title description])) false true))

      ; this field is included to run filters on
      (.add (create-field "category" category))

      ; these fields are just stored in order to be able to display
      ; them in the search results without loading the document
      ; from a database
      (.add (create-field "title" title false))
      (.add (create-field "description" description false)))))

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
        (TermQuery. (Term. "category" (str "\"" category "\"")))))))

(defn search [query query-filter limit reader analyzer]
  (let [searcher (IndexSearcher. reader)
        parser (QueryParser. (. Version LUCENE_30) "fulltext" analyzer)
        q (.parse parser query)
        top-docs (if-not (nil? query-filter)
                   (.search searcher q query-filter limit (new Sort))
                   (.search searcher q limit))]

    ; prints #<QueryWrapperFilter QueryWrapperFilter(category:"clojure")>
    ; for the test case in test-search
    (when-not (nil? query-filter) (println query-filter))

    (. searcher close)

    {:total-hits (.totalHits top-docs)
     :docs (.scoreDocs top-docs)}))

