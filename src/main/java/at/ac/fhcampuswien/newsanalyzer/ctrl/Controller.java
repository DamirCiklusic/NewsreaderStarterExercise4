package at.ac.fhcampuswien.newsanalyzer.ctrl;

import at.ac.fhcampuswien.newsanalyzer.downloader.ParallelDownloader;
import at.ac.fhcampuswien.newsanalyzer.downloader.SequentialDownloader;
import at.ac.fhcampuswien.newsapi.NewsApi;
import at.ac.fhcampuswien.newsapi.beans.Article;
import at.ac.fhcampuswien.newsapi.beans.NewsResponse;
import at.ac.fhcampuswien.newsapi.beans.Source;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Controller {

	public static final String APIKEY = "94647264d6f6447db56483291f42e511";  //0038b5ccc1124e94b01d19b0d5982697

	private List<Article> articles = null;

	public String process(NewsApi newsApi) throws NewsAPIException, IllegalArgumentException{
		System.out.println("Start process");

		if(newsApi == null)
			throw new IllegalArgumentException();

		articles = getArticles(newsApi);

		System.out.println("End process");

		return getArticlesPrintReady();
	}

	public List<Article> getArticles(NewsApi newsApi) throws NewsAPIException {
		NewsResponse newsResponse = newsApi.getNews();

		if(!newsResponse.getStatus().equals("ok")){
			throw new NewsAPIException("News Response returned status " + newsResponse.getStatus());
		}
		
		return newsResponse.getArticles();
	}

	private String getArticlesPrintReady() {
		return articles.stream()
				.map(Article::print)
				.collect(Collectors.joining("\n"));
	}

	public long getArticleCount() throws NewsAPIException {
		if(articles == null)
			throw new NewsAPIException("Load articles first");
		return articles.size();
	}

	public String getSortArticlesByLongestTitle() throws NewsAPIException {
		if(articles == null)
			throw new NewsAPIException("Load articles first");
		return articles.stream()
				.map(Article::getTitle)
				.filter(Objects::nonNull)
				.sorted(Comparator.comparing(String::length).reversed())
				.collect(Collectors.joining("\n"));
	}

	public String getShortestNameOfAuthors() throws NewsAPIException {
		if(articles == null)
			throw new NewsAPIException("Load data first");

		return articles.stream()
				.map(Article::getAuthor)
				.filter(Objects::nonNull)
				.min(Comparator.comparing(String::length))
				.orElseThrow();
	}

	public String getProviderWithMostArticles() throws NewsAPIException {
		if(articles == null)
			throw new NewsAPIException("Load data first");

		return articles.stream()
				.map(Article::getSource)
				.collect(Collectors.groupingBy(Source::getName))
				.entrySet()
				.stream()
				.max(Comparator.comparingInt(o -> o.getValue().size()))
				.map(stringListEntry -> stringListEntry.getKey() + " " + stringListEntry.getValue().size())
				.orElseThrow();
	}

	public List<String> getUrlList() throws NewsAPIException{
		if(articles == null) {
			throw new NewsAPIException("Load data first");
		}
		List<Article> articleList = articles;
		Stream<Article> streamFromList = articleList.stream();
		List<String> urlList = new ArrayList<>();

		streamFromList
				.filter( article -> article.getUrl() != null)
				.forEach( article -> urlList.add(article.getUrl()));
		return urlList;
	}

	public void downloadLastSearch() throws NewsAPIException{
		List<String> urlList = getUrlList();
		SequentialDownloader sqD = new SequentialDownloader();
		if(urlList == null){
			throw new NewsAPIException("Load data first");
		}
		int counter = sqD.process(urlList);
		System.out.println(counter + " articles successful downloaded.");
	}

	public void downloadLastSearchParallel() throws NewsAPIException{
		List<String> urlList = getUrlList();
		ParallelDownloader pqD = new ParallelDownloader();
		if(urlList == null){
			throw new NewsAPIException("Load data first");
		}
		int counter = pqD.process(urlList);
		System.out.println(counter + " articles successful downloaded.");
	}
}
