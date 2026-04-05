/* ══════════════════════════════════════════════
   Vetri Files — File Helper
   Retrieves file from sessionStorage or IndexedDB
   Use this in every tool page
══════════════════════════════════════════════ */

/* ── Get file blob ready for FormData ── */
function getFileBlob(callback) {
  const storageType = sessionStorage.getItem('vetri_storage_type');
  const stored      = sessionStorage.getItem('vetri_file');

  if (!stored) {
    alert('No file found. Please upload from home page.');
    window.location.href = '/';
    return;
  }

  const fileMeta = JSON.parse(stored);

  if (storageType === 'indexeddb') {
    _readFromDB(function(file) {
      if (file) { callback(file, fileMeta); return; }
      alert('File not found. Please upload again.');
      window.location.href = '/';
    });
  } else {
    // Try sessionStorage first
    const dataURL = sessionStorage.getItem('vetri_file_data');
    if (dataURL) {
      fetch(dataURL)
        .then(r => r.blob())
        .then(blob => {
          const file = new File([blob],
            fileMeta.name || 'file',
            { type: fileMeta.type || blob.type }
          );
          callback(file, fileMeta);
        })
        .catch(() => {
          alert('Error reading file. Please upload again.');
          window.location.href = '/';
        });
    } else {
      // sessionStorage failed — try IndexedDB as fallback
      _readFromDB(function(file) {
        if (file) { callback(file, fileMeta); return; }
        alert('File not found. Please upload again.');
        window.location.href = '/';
      });
    }
  }
}

/* ── Get dataURL for preview (images only) ── */
function getFileDataURL(callback) {
  const storageType = sessionStorage.getItem('vetri_storage_type');

  if (storageType === 'indexeddb') {
    _readFromDB(function(file) {
      if (!file) { callback(null); return; }
      const reader = new FileReader();
      reader.onload = e => callback(e.target.result);
      reader.readAsDataURL(file);
    });
  } else {
    const dataURL = sessionStorage.getItem('vetri_file_data');
    if (dataURL) { callback(dataURL); return; }
    // Fallback to IndexedDB
    _readFromDB(function(file) {
      if (!file) { callback(null); return; }
      const reader = new FileReader();
      reader.onload = e => callback(e.target.result);
      reader.readAsDataURL(file);
    });
  }
}

/* ── Get video/audio object URL for player ── */
function getFileObjectURL(callback) {
  const storageType = sessionStorage.getItem('vetri_storage_type');
  const dataURL = sessionStorage.getItem('vetri_file_data');

  const tryIndexedDB = cb => {
    _readFromDB(function(file) {
      cb(file ? URL.createObjectURL(file) : null);
    });
  };

  const trySessionStorage = cb => {
    if (!dataURL) { cb(null); return; }
    fetch(dataURL)
      .then(r => r.blob())
      .then(blob => cb(URL.createObjectURL(blob)))
      .catch(() => cb(null));
  };

  if (storageType === 'indexeddb') {
    tryIndexedDB(url => {
      if (url) { callback(url); }
      else { trySessionStorage(callback); }
    });
  } else if (storageType === 'session') {
    trySessionStorage(url => {
      if (url) { callback(url); }
      else { tryIndexedDB(callback); }
    });
  } else {
    if (dataURL) {
      trySessionStorage(url => {
        if (url) { callback(url); }
        else { tryIndexedDB(callback); }
      });
    } else {
      tryIndexedDB(callback);
    }
  }
}

/* ── Internal: read file from IndexedDB ── */
function _readFromDB(callback) {
  try {
    const req = indexedDB.open('VetriFilesDB', 1);

    req.onerror = () => callback(null);

    req.onupgradeneeded = e => {
      const db = e.target.result;
      if (!db.objectStoreNames.contains('files')) {
        db.createObjectStore('files', { keyPath: 'id' });
      }
    };

    req.onsuccess = e => {
      const db = e.target.result;

      if (!db.objectStoreNames.contains('files')) {
        db.close();
        callback(null);
        return;
      }

      const tx      = db.transaction('files', 'readonly');
      const store   = tx.objectStore('files');
      const getReq  = store.get('current_file');

      getReq.onsuccess = ev => {
        db.close();
        const record = ev.target.result;
        callback(record && record.file ? record.file : null);
      };

      getReq.onerror = () => {
        db.close();
        callback(null);
      };
    };
  } catch(e) {
    callback(null);
  }
}

/* ── Format time helper ── */
function formatTime(secs) {
  if (isNaN(secs) || secs < 0) return '00:00.000';
  const h  = Math.floor(secs / 3600);
  const m  = Math.floor((secs % 3600) / 60);
  const s  = Math.floor(secs % 60);
  const ms = Math.round((secs % 1) * 1000);
  const hStr = h > 0 ? String(h).padStart(2,'0') + ':' : '';
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