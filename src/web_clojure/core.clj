(ns web-clojure.core
  (:require
    [compojure.core :as c]
    [ring.adapter.jetty :as j] 
    [hiccup.core :as h]
    [ring.middleware.params :as p]
    [ring.util.response :as r])
  (:gen-class))

(defonce server (atom nil))
(defonce book (atom []))

(c/defroutes app
  (c/GET "/" []
    (h/html [:html
             [:body
              [:form {:action "/newbook" :method "post"}
               [:input {:type "text" :placeholder "Book Title" :name "title"}]
               [:input {:type "text" :placeholder "Author" :name "author"}]
               [:input {:type "text" :placeholder "Genre" :name "genre"}]
               [:button {:type "submit"} "Input Book to Library"]]
              [:ol
               (map (fn [title]
                      [:li title])
                 @book)]]]))

  (c/POST "/newbook" request
    (let [params (get request :params)]
        name (get params "title")
        author (get params "author")
        genre (get params "genre")
        
        title (conj [name ", " author ", " genre])
      (swap! book conj title)
      (spit "title.edn" (pr-str @book))
      (r/redirect "/"))))

(defn -main []
  (try
    (let [book-str (slurp "title.edn")
          book-vec (read-string book-str)]
      (reset! book book-vec))
    (catch Exception _))
  (when @server
    (.stop @server))
  (let [app (p/wrap-params app)]
    (reset! server (j/run-jetty app {:port 3000 :join? false}))))
