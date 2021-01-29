var json = `[
	{
		"urls": "https://en.wikipedia.org/wiki/Main_Page",
		"images": "images/favicon/wiki.png",
		"title": "Wikipedia",
		"description": "A multilingual open-collaborative online encyclopedia",
		"language": "All language"
	},
    {
        "urls": "https://www.gutenberg.org/",
        "images": "images/favicon/gt.png",
        "title": "Project Gutenberg",
        "description": "A library of over 60,000 free eBooks",
        "language": "All language"
    },
	{
		"urls": "https://news.google.com",
		"images": "images/favicon/news.png",
		"title": "Google News",
		"description": "Comprehensive up-to-date aggregated news coverage.",
		"language": "All language"
	},
	{
		"urls": "https://techcrunch.com/",
		"images": "images/favicon/techcrunch.png",
		"title": "TechCrunch",
		"description": " Startup and Technology News",
		"language": "English"
	},
	{
		"urls": "https://lifehacker.com/",
		"images": "images/favicon/lifehacker.png",
		"title": "Lifehacker",
		"description": "Do everything better",
		"language": "English"
	},
	{
		"urls": "https://news.ycombinator.com/",
		"images": "images/favicon/hn.ico",
		"title": "Hacker News",
		"description": "Social news website focusing on computer science and entrepreneurship",
		"language": "English"
	},
	{
		"urls": "https://www.nytimes.com/",
		"images": "images/favicon/nytimes.png",
		"title": "The New York Times",
		"description": "Breaking News, US News, World News",
		"language": "English"
	},
	{
		"urls": "https://www.wsj.com/",
		"images": "images/favicon/wsj.png",
		"title": "The Wall Street Journal",
		"description": "Breaking News, Business, Financial ...",
		"language": "English"
	},
	{
		"urls": "https://www.bloomberg.com",
		"images": "images/favicon/bloomberg.png",
		"title": "Bloomberg",
		"description": "Business and markets news, data, analysis, and video",
		"language": "English"
	},
	{
		"urls": "https://www.bbc.com/",
		"images": "images/favicon/bbc.jpg",
		"title": "BBC",
		"description": "Breaking news, sport, TV, radio and a whole lot more",
		"language": "English"
	},
	{
		"urls": "https://www.forbes.com/",
		"images": "images/favicon/forbes.png",
		"title": "Forbes",
		"description": "Global media company, focusing on business, investing, technology, Entrepreneurship, leadership, and lifestyle",
		"language": "English"
	},
	{
		"urls": "https://timesofindia.indiatimes.com/",
		"images": "images/favicon/toi.ico",
		"title": "Times of India",
		"description": "Latest News, Breaking News, Bollywood, Sports, Business and Political News",
		"language": "English"
	},
	{
		"urls": "https://www.thehindu.com/",
		"images": "images/favicon/th.png",
		"title": "The Hindu",
		"description": "Breaking News, India News, Sports News and Live Updates",
		"language": "English"
	},
	{
		"urls": "https://www.scmp.com/",
		"images": "images/favicon/scmp.png",
		"title": "South China Morning Post",
		"description": "HK, China, Asia news opinion from SCMP's global edition",
		"language": "English"
	},
	{
		"urls": "https://cn.engadget.com/",
		"images": "images/favicon/engadget.png",
		"title": "Engadget",
		"description": "Consumer Electronics News and Reviews",
		"language": "Chinese"
	}
]`;


json = JSON.parse(json);

var html = "";
for (i=0; i<json.length; i++) {
    //console.log(json[i]);
    html += '<div class="container-fluid">'+
    '<div class="row">' +
        '<div class="col-12 mt-3">' +
            '<div class="card" onclick="window.open('+ "'" +json[i]['urls'] + "'" +')">' + 
                '<div class="card-horizontal">' +
                    '<div class="img-square-wrapper">' +
                        '<img class="" src="'+ json[i]['images'] +'" alt="Card image cap">' +
                    '</div>' +
                    '<div class="card-body">' +
                        '<h4 class="card-title">'+ json[i]['title'] +'</h4>' +
                        '<p class="card-text text">'+ json[i]['description'] +'</p>' +
                    '</div>' +
                '</div>' +
                '<div class="card-footer">' +
                    '<small class="text-muted">'+ json[i]['language'] +'</small>' +
                '</div>' +
            '</div>' +
        '</div>' +
    '</div>' +
'</div>';
}

window.onload = function () {
    document.getElementById("container").innerHTML = html;
}

