/* ══════════════════════════════════════════════
   Vetri Files — File Helper
   Retrieves file from sessionStorage or IndexedDB
   Use this in every tool page
══════════════════════════════════════════════ */

/* ── Get file blob ready for FormData ── */
function getFileBlob(callback) {
  const storageType =
    sessionStorage.getItem('vetri_storage_type');
  const stored =
    sessionStorage.getItem('vetri_file');

  if (!stored) {
    alert('No file found. Please upload from home page.');
    window.location.href = '/';
    return;
  }

  const fileMeta = JSON.parse(stored);

  if (storageType === 'indexeddb') {
    // Get from IndexedDB
    const request = indexedDB.open('VetriFilesDB', 1);
    request.onsuccess = e => {
      const db    = e.target.result;
      const tx    = db.transaction('files', 'readonly');
      const store = tx.objectStore('files');
      const getReq = store.get('current_file');
      getReq.onsuccess = ev => {
        const record = ev.target.result;
        if (record && record.file) {
          db.close();
          callback(record.file, fileMeta);
        } else {
          db.close();
          alert('File not found. Please upload again.');
          window.location.href = '/';
        }
      };
      getReq.onerror = () => {
        db.close();
        alert('Error reading file. Please upload again.');
        window.location.href = '/';
      };
    };
    request.onerror = () => {
      alert('Database error. Please upload again.');
      window.location.href = '/';
    };
  } else {
    // Get from sessionStorage dataURL
    const dataURL =
      sessionStorage.getItem('vetri_file_data');
    if (!dataURL) {
      alert('File data not found. Please upload again.');
      window.location.href = '/';
      return;
    }
    fetch(dataURL)
      .then(r => r.blob())
      .then(blob => {
        const name = fileMeta.name || 'file';
        const type = fileMeta.type || blob.type || 'application/octet-stream';
        const file = new File([blob], name, { type });
        callback(file, fileMeta);
      })
      .catch(() => {
        alert('Error reading file. Please upload again.');
        window.location.href = '/';
      });
  }
}

/* ── Get dataURL for preview (images only) ── */
function getFileDataURL(callback) {
  const storageType =
    sessionStorage.getItem('vetri_storage_type');

  if (storageType === 'indexeddb') {
    const request = indexedDB.open('VetriFilesDB', 1);
    request.onsuccess = e => {
      const db    = e.target.result;
      const tx    = db.transaction('files', 'readonly');
      const store = tx.objectStore('files');
      const getReq = store.get('current_file');
      getReq.onsuccess = ev => {
        const record = ev.target.result;
        if (record && record.file) {
          db.close();
          const reader = new FileReader();
          reader.onload = re =>
            callback(re.target.result);
          reader.readAsDataURL(record.file);
        }
      };
    };
  } else {
    const dataURL =
      sessionStorage.getItem('vetri_file_data');
    if (dataURL) callback(dataURL);
  }
}

/* ── Get video/audio object URL for player ── */
function getFileObjectURL(callback) {
  const storageType =
    sessionStorage.getItem('vetri_storage_type');

  if (storageType === 'indexeddb') {
    const request = indexedDB.open('VetriFilesDB', 1);
    request.onerror = () => callback(null);
    request.onsuccess = () => {
      const db = request.result;
      if (!db.objectStoreNames.contains('files')) {
        db.close();
        callback(null);
        return;
      }
      const tx = db.transaction('files', 'readonly');
      const getReq = tx.objectStore('files').get('current_file');
      getReq.onsuccess = () => {
        const record = getReq.result;
        if (record && record.file) {
          db.close();
          callback(URL.createObjectURL(record.file));
        } else {
          db.close();
          callback(null);
        }
      };
      getReq.onerror = () => {
        db.close();
        callback(null);
      };
    };
  } else {
    const dataURL =
      sessionStorage.getItem('vetri_file_data');
    callback(dataURL || null);
  }
}

/* ── Format time helper ── */
function formatTime(secs) {
  if (isNaN(secs) || secs < 0) return '00:00.000';
  const h   = Math.floor(secs / 3600);
  const m   = Math.floor((secs % 3600) / 60);
  const s   = Math.floor(secs % 60);
  const ms  = Math.round((secs % 1) * 1000);
  const hStr = h > 0 ? h + ':' : '';
  return hStr +
    String(m).padStart(2,'0') + ':' +
    String(s).padStart(2,'0') + '.' +
    String(ms).padStart(3,'0');
}

/* ── Format file size ── */
function formatSize(bytes) {
  if (!bytes) return '—';
  if (bytes < 1024) return bytes + ' B';
  if (bytes < 1024 * 1024)
    return (bytes / 1024).toFixed(1) + ' KB';
  if (bytes < 1024 * 1024 * 1024)
    return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
  return (bytes / (1024 * 1024 * 1024)).toFixed(2) + ' GB';
}