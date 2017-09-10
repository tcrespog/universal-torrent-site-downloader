function markAsLoggedIn() {
    localStorage.setItem('logged', true);
}

function isLoggedIn() {
    return (localStorage.getItem('logged') === true);
}

function writeLoggedInSection() {
    document.write('<p class="logged-indicator">This text appears if you are logged in</p>');
}

function registerVisitNumber() {
    var nVisits = getVisitNumber();

    localStorage.setItem('nVisits', ++nVisits);
}

function getVisitNumber() {
    return localStorage.getItem('nVisits') || 0;
}

function mustShowDesiredResult() {
    return /showResult=true/.test(document.location.search) || (getVisitNumber() > 2);
}

function writeDesiredResult() {
    document.write('<li><a href="detail.html">The torrent you are looking for</a></li>')
}