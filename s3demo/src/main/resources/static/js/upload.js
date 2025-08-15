document.addEventListener('DOMContentLoaded', () => {
    const uploadButton = document.getElementById('upload-button');
    if (!uploadButton) {
        // Button doesn't exist on this page, so don't add a listener.
        return;
    }

    uploadButton.addEventListener('click', async () => {
        const fileInput = document.getElementById('file-input');
        const file = fileInput.files[0];
        if (!file) {
            alert('Please select a file first.');
            return;
        }
        console.log('File selected:', file);

        const fileName = file.name;
        const fileFingerprint = `${file.name}-${file.size}-${file.lastModified}`;
        console.log('File fingerprint:', fileFingerprint);
    const chunkSize = 5 * 1024 * 1024; // 5MB
    const totalChunks = Math.ceil(file.size / chunkSize);

    // Store both uploadId and objectKey for resuming
    let uploadInfo = null;
    try {
        const storedInfo = localStorage.getItem(fileFingerprint);
        if (storedInfo) {
            uploadInfo = JSON.parse(storedInfo);
        }
    } catch (e) {
        console.error('Failed to parse upload info from localStorage:', e);
        localStorage.removeItem(fileFingerprint); // Clean up corrupted data
    }
    let uploadId = uploadInfo ? uploadInfo.uploadId : null;
    let objectKey = uploadInfo ? uploadInfo.objectKey : null;

    let uploadedParts = [];

    const status = document.getElementById('status');
    const progress = document.getElementById('progress');

    function updateProgress(value) {
        progress.style.width = value + '%';
    }

    status.textContent = 'Starting upload...';

    try {
        console.log('Starting upload process...');
        // Check if we have a resumable upload
        if (uploadId && objectKey) {
            status.textContent = `Resuming upload for ${objectKey}`;
            try {
                // Use objectKey to list parts
                const listPartsResponse = await axios.get(`/upload/parts?objectKey=${objectKey}&uploadId=${uploadId}`);
                uploadedParts = listPartsResponse.data.map(part => ({
                    partNumber: part.partNumber,
                    eTag: part.etag
                }));
                status.textContent = `Resumed upload. ${uploadedParts.length} parts already uploaded.`;
            } catch (error) {
                if (error.response && error.response.status === 404) {
                    status.textContent = 'Previous upload not found on server. Starting a new upload.';
                    localStorage.removeItem(fileFingerprint); // Clean up invalid record
                    uploadId = null;
                    objectKey = null;
                } else {
                    throw error; // Re-throw other errors
                }
            }
        }

        // If no valid uploadId, initiate a new one
        if (!uploadId || !objectKey) {
            status.textContent = 'Initiating new upload...';
            const initiateResponse = await axios.post(`/upload/initiate?fileName=${fileName}`);
            
            // The backend now returns an object with uploadId and objectKey
            uploadId = initiateResponse.data.uploadId;
            objectKey = initiateResponse.data.objectKey;

            // Save both to localStorage for resuming
            localStorage.setItem(fileFingerprint, JSON.stringify({ uploadId, objectKey }));
            status.textContent = `New upload initiated. Object Key: ${objectKey}`;
        }

        // 2. Upload Chunks
        for (let i = 0; i < totalChunks; i++) {
            const partNumber = i + 1;

            // Skip already uploaded parts by checking our standardized list
            if (uploadedParts.some(p => p.partNumber === partNumber)) {
                status.textContent = `Skipping already uploaded part ${partNumber}`;
                updateProgress((partNumber / totalChunks) * 100);
                continue;
            }

            const start = i * chunkSize;
            const end = Math.min(start + chunkSize, file.size);
            const chunk = file.slice(start, end);

            const formData = new FormData();
            formData.append('file', chunk);
            // Use objectKey instead of fileName for all subsequent requests
            formData.append('objectKey', objectKey);
            formData.append('uploadId', uploadId);
            formData.append('partNumber', partNumber);

            status.textContent = `Uploading part ${partNumber} of ${totalChunks}...`;

            const partResponse = await axios.post('/upload/part', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            });

            // The backend returns a PartETag which serializes to partNumber and etag
            uploadedParts.push({
                partNumber: partNumber,
                eTag: partResponse.data.etag
            });
            updateProgress((partNumber / totalChunks) * 100);
        }

        // 3. Complete Multipart Upload
        status.textContent = 'Completing upload...';
        
        // Use objectKey to complete the upload
        await axios.post(`/upload/complete?objectKey=${objectKey}&uploadId=${uploadId}`, uploadedParts, {
            headers: {
                'Content-Type': 'application/json'
            }
        });

        status.textContent = 'File uploaded successfully!';
        updateProgress(100);
        localStorage.removeItem(fileFingerprint); // Clean up localStorage

    } catch (error) {
        console.error('Upload failed:', error);
        status.textContent = 'Upload failed. See console for details.';
    }
    });
});
