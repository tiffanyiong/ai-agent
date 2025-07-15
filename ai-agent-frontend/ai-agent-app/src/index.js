import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css'; // 引入 Tailwind CSS 樣式
import App from './App.jsx'; // 引入您的主元件，注意副檔名是 .jsx

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
    <React.StrictMode>
        <App />
    </React.StrictMode>
);
