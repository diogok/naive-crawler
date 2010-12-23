(ns naive-crawler.core
    (:use clojure.contrib.io) )

    (defn safe [fun]
     "Safe call"
      (try (fun) (catch Exception e (do false))))

    (defn make-url [base path]
     "Make a absolute URL taking the current url
     and the next url. Return is a String"
     (or (safe #(.toString (java.net.URL. (java.net.URL. base) path))) base))

    (defn find-links [page]
     "Return the links found on a page as a list"
     (map #(-> % rest second) (re-seq #"href=('|\")([^'\"]+)('|\")" page)) )

    (defn find-urls [base page]
     "Return the aboslute urls for the page at base url"
     (map #(make-url base %1) (find-links page)))

    (defn same-domain? [current next]
     "Return true if both urls are from same domain"
     (= (.getHost (java.net.URL. current)) (.getHost (java.net.URL. next)))) 

    (defn make-db []
     "Start an empy database"
     (atom {})) 

    (defn insert [db url]
     "Insert an url on the db, if no there already"
     (swap! db #(if (nil? (get %1 url)) (assoc %1 url {}) %1))) 

    (defn get-it [db url]
     "Get the url from db"
     (get @db url)) 

    (defn non-crawled [db]
     "Return urls not crawled"
     (map first (filter #(nil? (:content (second %1))) @db)))

    (defn set-content [db page content]
     "Set the content of a page on the db"
     (:content (swap! db #(assoc %1 page {:url page :content content}))))

    (defn save-page [db page]
     "Get the content of page, save it and add it's urls to be crawled"
     (let [content (or (safe #(slurp* page)) "ERROR")]
      (set-content db page content)
      (dorun (map #(insert db %1) (filter (partial same-domain? page) (find-urls page content))))))

    (defn start [url]
     "Start the crawler"
     (let [db (make-db )]
      (insert db url) 
      (while (not (empty? (non-crawled db)))
       (dorun (pmap #(save-page db %1) (take n (non-crawled db )))))
      (deref db))) 

    (defn -main [url]
     (start url)) 
