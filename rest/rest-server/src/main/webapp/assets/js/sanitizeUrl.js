try {
    var urlObject = new URL(window.location.href);
    var url = urlObject.href;
    const sanitizedUrl = url.replace(/[^\w\s-:.\/@]/gi, "");
    url = sanitizedUrl.substring(0, (sanitizedUrl.indexOf("#") == -1) ? sanitizedUrl.length : sanitizedUrl.indexOf("#"));
    url = url.substring(0, (sanitizedUrl.indexOf("?") == -1) ? sanitizedUrl.length : sanitizedUrl.indexOf("?"));
    url = url.substring(0, sanitizedUrl.lastIndexOf("/"));
    var restApiEndpoint = sanitizedUrl;
    document.getElementById('restUrlCli').textContent = restApiEndpoint;
    var date = document.getElementById("current-year");
    date.innerText = (new Date()).getFullYear();
} catch (err) {
    console.error('Error:', err);
}