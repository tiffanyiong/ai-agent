import React, { useState, useEffect, useRef } from 'react';

// --- Helper & Mock Data ---
const API_BASE_URL = 'http://localhost:8123/api';

// --- SVG Icons ---
const HomeIcon = ({ className }) => (
    <svg className={className} xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
        <path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8h5z" />
    </svg>
);

const SendIcon = ({ className }) => (
    <svg className={className} xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
        <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z" />
    </svg>
);

const BotIcon = () => (
    <div className="w-8 h-8 rounded-full bg-gradient-to-tr from-indigo-500 to-purple-500 flex items-center justify-center text-white font-bold text-sm shadow-md flex-shrink-0">
        AI
    </div>
);

const UserIcon = () => (
    <div className="w-8 h-8 rounded-full bg-gradient-to-tr from-sky-500 to-cyan-400 flex items-center justify-center text-white font-bold text-sm shadow-md flex-shrink-0">
        U
    </div>
);

// 【NEW】: Added icons for the home page cards
const CameraIcon = ({ className }) => (
    <svg className={className} xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M14.5 4h-5L7 7H4a2 2 0 0 0-2 2v9a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2h-3l-2.5-3z"></path>
        <circle cx="12" cy="13" r="3"></circle>
    </svg>
);

const ChipIcon = ({ className }) => (
    <svg className={className} xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <rect x="3" y="3" width="18" height="18" rx="2"></rect>
        <rect x="7" y="7" width="10" height="10" rx="1"></rect>
        <path d="M7 1v2"></path><path d="M17 1v2"></path>
        <path d="M1 7h2"></path><path d="M1 17h2"></path>
        <path d="M21 7h2"></path><path d="M21 17h2"></path>
        <path d="M7 21v2"></path><path d="M17 21v2"></path>
    </svg>
);


// --- Components ---

/**
 * Message Bubble Component
 * @param {object} props - Contains message object { id, text, sender }
 */
const ChatBubble = ({ message }) => {
    const isUser = message.sender === 'user';

    const containerClasses = isUser
        ? 'self-end flex-row-reverse'
        : 'self-start flex-row';

    const bubbleColorClass = isUser ? 'bg-sky-500/30' : 'bg-gray-700/40';

    const formatText = (text) => {
        return text.replace(/\n/g, '<br />');
    };

    return (
        <div className={`flex items-start gap-3 max-w-xl ${containerClasses}`}>
            {isUser ? <UserIcon /> : <BotIcon />}
            <div className={`rounded-2xl px-4 py-3 text-white shadow-lg backdrop-blur-sm ${bubbleColorClass}`}>
                <p className="text-base" dangerouslySetInnerHTML={{ __html: formatText(message.text) }} />
            </div>
        </div>
    );
};

/**
 * Chat Page Component
 * @param {object} props - Contains title, apiEndpoint, useChatId, onBack
 */
const ChatPage = ({ title, apiEndpoint, useChatId, onBack }) => {
    const [messages, setMessages] = useState([]);
    const [inputValue, setInputValue] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [chatId, setChatId] = useState(null);
    const eventSourceRef = useRef(null);
    const chatContainerRef = useRef(null);

    // Generate chatId on page entry (if needed)
    useEffect(() => {
        if (useChatId) {
            const newChatId = crypto.randomUUID();
            setChatId(newChatId);
            setMessages([{
                id: Date.now(),
                text: `Hello! I'm your AI Photography Assistant. A new session has started.`,
                sender: 'ai'
            }]);
        } else {
            setMessages([{
                id: Date.now(),
                text: `Hello! I'm your AI Super Agent. How can I assist you today?`,
                sender: 'ai'
            }]);
        }
    }, [useChatId]);

    // Auto-scroll to bottom when messages update
    useEffect(() => {
        if (chatContainerRef.current) {
            chatContainerRef.current.scrollTop = chatContainerRef.current.scrollHeight;
        }
    }, [messages]);

    // Cleanup function to close SSE connection on component unmount
    useEffect(() => {
        return () => {
            if (eventSourceRef.current) {
                eventSourceRef.current.close();
            }
        };
    }, []);

    const handleSendMessage = (e) => {
        e.preventDefault();
        if (!inputValue.trim() || isLoading) return;

        const userMessage = {
            id: Date.now(),
            text: inputValue,
            sender: 'user',
        };
        setMessages(prev => [...prev, userMessage]);
        setInputValue('');
        setIsLoading(true);

        if (eventSourceRef.current) {
            eventSourceRef.current.close();
        }

        const params = new URLSearchParams();
        params.append('message', inputValue);
        if (useChatId && chatId) {
            params.append('chatId', chatId);
        }
        const url = `${API_BASE_URL}${apiEndpoint}?${params.toString()}`;

        const newEventSource = new EventSource(url);
        eventSourceRef.current = newEventSource;

        let aiMessageId = null;

        newEventSource.onmessage = (event) => {
            const data = event.data;

            setMessages(prev => {
                if (aiMessageId === null) {
                    aiMessageId = Date.now();
                    setIsLoading(false);
                    return [...prev, { id: aiMessageId, text: data, sender: 'ai' }];
                }
                return prev.map(msg =>
                    msg.id === aiMessageId ? { ...msg, text: msg.text + data } : msg
                );
            });
        };

        newEventSource.onerror = () => {
            setIsLoading(false);
            newEventSource.close();
            eventSourceRef.current = null;
        };
    };

    return (
        <div className="flex flex-col h-full w-full bg-transparent overflow-hidden">
            <header className="flex items-center justify-between p-4 text-white bg-black/20 backdrop-blur-sm shadow-lg border-b border-white/10 flex-shrink-0">
                <button onClick={onBack} className="p-2 rounded-full hover:bg-white/10 transition-colors">
                    <HomeIcon className="w-6 h-6" />
                </button>
                <h1 className="text-xl font-bold">{title}</h1>
                {useChatId && chatId ? (
                    <div className="text-right">
                        <span className="text-xs text-gray-400 font-mono select-all" title={chatId}>
                            Session ID: {chatId.substring(0, 8)}...
                        </span>
                    </div>
                ) : (
                    <div className="w-10"></div>
                )}
            </header>

            <main ref={chatContainerRef} className="flex flex-col flex-1 p-4 md:p-6 space-y-6 overflow-y-auto">
                {messages.map((msg) => (
                    <ChatBubble key={msg.id} message={msg} />
                ))}
                {isLoading && (
                    <div className="flex items-start gap-3 max-w-xl self-start">
                        <BotIcon />
                        <div className="rounded-2xl px-4 py-3 text-white bg-gray-700/40 self-start backdrop-blur-sm shadow-lg">
                            <div className="flex items-center space-x-2">
                                <div className="w-2 h-2 bg-white rounded-full animate-pulse delay-0"></div>
                                <div className="w-2 h-2 bg-white rounded-full animate-pulse delay-200"></div>
                                <div className="w-2 h-2 bg-white rounded-full animate-pulse delay-400"></div>
                            </div>
                        </div>
                    </div>
                )}
            </main>

            <footer className="p-4 md:p-6 bg-black/20 backdrop-blur-sm border-t border-white/10 flex-shrink-0">
                <form onSubmit={handleSendMessage} className="flex items-center gap-4">
                    <input
                        type="text"
                        value={inputValue}
                        onChange={(e) => setInputValue(e.target.value)}
                        placeholder="Type your message here..."
                        className="flex-1 w-full bg-gray-800/50 text-white placeholder-gray-400 px-4 py-3 rounded-full border border-transparent focus:outline-none focus:ring-2 focus:ring-purple-500 transition-all"
                        disabled={isLoading}
                    />
                    <button
                        type="submit"
                        disabled={isLoading || !inputValue.trim()}
                        className="p-3 rounded-full bg-gradient-to-br from-purple-600 to-indigo-600 text-white disabled:opacity-50 disabled:cursor-not-allowed hover:scale-110 transition-transform"
                    >
                        <SendIcon className="w-6 h-6" />
                    </button>
                </form>
            </footer>
        </div>
    );
};

/**
 * Home Page Component
 * @param {object} props - Contains setPage function
 */
const HomePage = ({ setPage }) => {
    // 【MODIFIED】: Card component now accepts an 'icon' prop
    const Card = ({ title, description, onClick, gradient, icon: Icon }) => (
        <div
            onClick={onClick}
            className={`relative p-8 rounded-2xl shadow-2xl cursor-pointer overflow-hidden group transition-all duration-500 hover:scale-105 bg-gradient-to-br ${gradient}`}
        >
            {/* 【NEW】: Render the icon as a large, semi-transparent background element */}
            {Icon && <Icon className="absolute -right-4 -bottom-4 h-32 w-32 text-white/10 group-hover:text-white/20 transition-colors duration-500 transform group-hover:rotate-[-10deg]"/>}

            <div className="absolute inset-0 bg-black/30 group-hover:bg-black/10 transition-colors duration-500"></div>
            <div className="relative z-10">
                <h2 className="text-3xl font-bold text-white mb-2">{title}</h2>
                <p className="text-gray-200">{description}</p>
            </div>
        </div>
    );

    return (
        <div className="w-full h-full flex flex-col items-center justify-center p-8 text-center">
            <h1 className="text-5xl md:text-7xl font-extrabold text-white mb-4">
                <span className="bg-clip-text text-transparent bg-gradient-to-r from-purple-400 to-pink-600">
                    AI Application Hub
                </span>
            </h1>
            <p className="text-xl text-gray-300 mb-12 max-w-2xl">
                Choose an AI assistant to begin your experience. Whether it's for photography consultation or a super agent, we have you covered.
            </p>
            <div className="grid md:grid-cols-2 gap-8 w-full max-w-4xl">
                {/* 【MODIFIED】: Passed the icon components to the Card */}
                <Card
                    title="AI Photography Assistant"
                    description="Your professional photography consultant for booking and advice."
                    onClick={() => setPage('photo')}
                    gradient="from-sky-500 to-indigo-500"
                    icon={CameraIcon}
                />
                <Card
                    title="AI Super Agent"
                    description="A powerful, general-purpose agent capable of handling complex tasks."
                    onClick={() => setPage('manus')}
                    gradient="from-purple-500 to-pink-500"
                    icon={ChipIcon}
                />
            </div>
        </div>
    );
};


/**
 * Main Application Component
 */
export default function App() {
    const [page, setPage] = useState('home');

    const renderPage = () => {
        switch (page) {
            case 'photo':
                return <ChatPage
                    title="AI Photography Assistant"
                    apiEndpoint="/ai/photo_consult/chat"
                    useChatId={true}
                    onBack={() => setPage('home')}
                />;
            case 'manus':
                return <ChatPage
                    title="AI Super Agent"
                    apiEndpoint="/ai/manus/chat"
                    useChatId={false}
                    onBack={() => setPage('home')}
                />;
            default:
                return <HomePage setPage={setPage} />;
        }
    };

    return (
        <div className="h-screen w-screen bg-gray-900 font-sans">
            {/* Background Gradient Effect */}
            <div className="absolute inset-0 z-0">
                <div className="absolute bottom-0 left-[-20%] right-0 top-[-10%] h-[500px] w-[500px] rounded-full bg-[radial-gradient(circle_farthest-side,rgba(255,0,182,.15),rgba(255,255,255,0))]"></div>
                <div className="absolute bottom-0 right-[-20%] top-[-10%] h-[500px] w-[500px] rounded-full bg-[radial-gradient(circle_farthest-side,rgba(255,0,182,.15),rgba(255,255,255,0))]"></div>
            </div>

            {/* Main Content Container */}
            <div className="relative z-10 h-full w-full flex items-center justify-center">
                <div className="h-full w-full md:h-[90vh] md:w-[90vw] md:max-w-4xl lg:max-w-5xl bg-black/20 rounded-none md:rounded-2xl shadow-2xl border border-white/10 backdrop-blur-xl overflow-hidden">
                    {renderPage()}
                </div>
            </div>
        </div>
    );
}
