document.addEventListener('DOMContentLoaded', function() {
    const video = document.getElementById('videoPlayer');
    const videoList = document.getElementById('videoList');

    fetch('/video/list')
        .then(response => response.json())
        .then(videoInfos => {
            videoInfos.forEach(videoInfo => {
                const option = document.createElement('option');
                option.value = videoInfo.objectKey;
                option.textContent = videoInfo.fileName;
                videoList.appendChild(option);
            });

            if (videoInfos.length > 0) {
                loadVideo(videoInfos[0].objectKey);
            }
        })
        .catch(error => console.error('Error fetching video list:', error));

    videoList.addEventListener('change', function() {
        const selectedVideo = videoList.value;
        if (selectedVideo) {
            loadVideo(selectedVideo);
        }
    });

    function loadVideo(objectKey) {
        const encodedObjectKey = encodeURIComponent(objectKey);
        const videoSrc = `/video/stream?objectKey=${encodedObjectKey}`;
        console.log('Setting video source to:', videoSrc);
        video.src = videoSrc;
    }

    video.addEventListener('error', function(e) {
        console.error('Error loading video:', e);
    });
});
