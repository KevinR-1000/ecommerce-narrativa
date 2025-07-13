const CACHE_NAME = 'narrativa-cache-v1';
const urlsToCache = [

    '/', // La raíz de la aplicación
    '/Eccomerce/index',
    '/Eccomerce/ficcion',
    '/Eccomerce/conocimiento',
    '/Eccomerce/jovenes',
    '/Eccomerce/login',
    // Archivos CSS
    '/css/styles.css',
    '/css/nosotros.css',
    '/css/login.css',
    '/css/productos.css',
    '/css/estilo.css',

    // Archivos JavaScript
    '/js/jsindex.js',
    '/js/javascript.js',
    '/js/login.js',

    // Imágenes y logos
    '/img/logoprincipal.jpg',
    '/img/narrativalogo.jpg',
    '/img/libreriaportada.jpg',
    '/img/banner1.jpg',
    '/img/cienciayficcion.jpg',
    '/img/desarrollo2.jpg',
    '/img/niñoslibros.jpg',


    // Iconos SVG
    '/icons/icon-user.svg',
    '/icons/icon-facebook.svg',
    '/icons/icon-youtube.svg',
    '/icons/icon-instagram.svg',

    // Iconos PNG para el Manifest y Apple Touch Icon
    '/icons/icon-48x48.png',
    '/icons/icon-72x72.png',
    '/icons/icon-96x96.png',
    '/icons/icon-144x144.png',
    '/icons/icon-192x192.png',
    '/icons/icon-512x512.png',
    '/icons/apple-touch-icon.png',          // Para iOS
    '/icons/apple-touch-icon-180x180.png'  // Para iOS
];

self.addEventListener('install', event => {
    console.log('[Service Worker] Installing...');
    event.waitUntil(
        caches.open(CACHE_NAME)
            .then(cache => {
                console.log('[Service Worker] Caching all app shell');
                return cache.addAll(urlsToCache);
            })
            .catch(error => {
                console.error('[Service Worker] Cache addAll failed:', error);
            })
    );
});

self.addEventListener('fetch', event => {
    event.respondWith(
        caches.match(event.request)
            .then(response => {
                // Cache hit - return response
                if (response) {
                    console.log('[Service Worker] Serving from cache:', event.request.url);
                    return response;
                }
                // No cache hit - fetch from network
                console.log('[Service Worker] Fetching from network:', event.request.url);
                return fetch(event.request);
            })
            .catch(error => {
                console.error('[Service Worker] Fetch failed:', error);

                // return caches.match('/offline.html');
            })
    );
});

self.addEventListener('activate', event => {
    console.log('[Service Worker] Activating...');
    const cacheWhitelist = [CACHE_NAME]; // Solo mantiene el caché actual
    event.waitUntil(
        caches.keys().then(cacheNames => {
            return Promise.all(
                cacheNames.map(cacheName => {
                    if (cacheWhitelist.indexOf(cacheName) === -1) {
                        console.log('[Service Worker] Deleting old cache:', cacheName);
                        return caches.delete(cacheName);
                    }
                })
            );
        })
    );
    // Reclamar clientes inmediatamente después de la activación
    return self.clients.claim();
});