import React, { useState, useEffect, useRef } from 'react';

// --- Constants ---
const API_BASE_URL = 'http://localhost:8080/api';
const API_ENDPOINT = '/ai/photo_consult/chat';

// --- Animations & Styles ---
const CustomStyles = () => (
    <style>{`
    @keyframes fadeIn {
      from { opacity: 0; transform: translateY(10px); }
      to { opacity: 1; transform: translateY(0); }
    }
    @keyframes popIn {
      0% { opacity: 0; transform: scale(0.8) translateY(10px); }
      80% { opacity: 1; transform: scale(1.05); }
      100% { opacity: 1; transform: scale(1); }
    }
    @keyframes blob-animation {
      0% { transform: translate(0px, 0px) scale(1); }
      33% { transform: translate(30px, -50px) scale(1.1); }
      66% { transform: translate(-20px, 20px) scale(0.9); }
      100% { transform: translate(0px, 0px) scale(1); }
    }
    .animate-fadeIn { animation: fadeIn 0.5s ease-out forwards; }
    .animate-popIn { animation: popIn 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275) forwards; }
    .animate-blob { animation: blob-animation 8s infinite ease-in-out; }
    .animation-delay-2000 { animation-delay: -2s; }
    .animation-delay-4000 { animation-delay: -4s; }
  `}</style>
);

// --- SVG Icons ---
const SendIcon = ({ className }) => (
    <svg className={className} xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
        <path d="M3.478 2.405a.75.75 0 00-.926.94l2.432 7.905H13.5a.75.75 0 010 1.5H4.984l-2.432 7.905a.75.75 0 00.926.94 60.519 60.519 0 0018.445-8.986.75.75 0 000-1.218A60.517 60.517 0 003.478 2.405z" />
    </svg>
);

const BotIcon = () => (
    <div className="w-11 h-11 rounded-full bg-gradient-to-br from-purple-400 to-pink-500 shadow-lg flex items-center justify-center flex-shrink-0 border-2 border-white/80">
        <span className="font-bold text-white text-lg">AI</span>
    </div>
);

const UserIcon = () => (
    <div className="w-11 h-11 rounded-full bg-white shadow-lg flex items-center justify-center flex-shrink-0">
        <span className="font-bold text-sky-500 text-lg">U</span>
    </div>
);

const FeatureCard = ({ icon, title, description, delay }) => (
    <div
        className="bg-white/60 p-4 rounded-2xl shadow-lg backdrop-blur-lg text-center animate-fadeIn group hover:-translate-y-2 transition-transform duration-300"
        style={{ animationDelay: `${delay}ms` }}
    >
        <div className="text-gray-500 group-hover:text-pink-500 transition-colors duration-300">
            {icon}
        </div>
        <p className="text-sm font-semibold text-gray-800 mt-3">{title}</p>
        <p className="text-xs text-gray-600 mt-1">{description}</p>
    </div>
);

const SimpleCalendarIcon = ({ className }) => (
    <svg className={className} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={2} stroke="currentColor">
        <path strokeLinecap="round" strokeLinejoin="round" d="M6.75 3v2.25M17.25 3v2.25M3 18.75V7.5a2.25 2.25 0 0 1 2.25-2.25h13.5A2.25 2.25 0 0 1 21 7.5v11.25m-18 0A2.25 2.25 0 0 0 5.25 21h13.5A2.25 2.25 0 0 0 21 18.75m-18 0h18M-4.5 12h22.5" />
    </svg>
);

const SimplePencilIcon = ({ className }) => (
    <svg className={className} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={2} stroke="currentColor">
        <path strokeLinecap="round" strokeLinejoin="round" d="m16.862 4.487 1.687-1.688a1.875 1.875 0 1 1 2.652 2.652L6.832 19.82a4.5 4.5 0 0 1-1.897 1.13l-2.685.8.8-2.685a4.5 4.5 0 0 1 1.13-1.897L16.863 4.487Zm0 0L19.5 7.125" />
    </svg>
);

const SimpleQuestionIcon = ({ className }) => (
    <svg className={className} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={2} stroke="currentColor">
        <path strokeLinecap="round" strokeLinejoin="round" d="M9.879 7.519c1.171-1.025 3.071-1.025 4.242 0 1.172 1.025 1.172 2.687 0 3.712-.203.179-.43.326-.67.442-.745.361-1.45.999-1.45 1.827v.75M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Zm-9 5.25h.008v.008H12v-.008Z" />
    </svg>
);

const InstagramIcon = ({ className }) => (
    <svg className={className} xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
        <path d="M12 2.163c3.204 0 3.584.012 4.85.07 3.252.148 4.771 1.691 4.919 4.919.058 1.265.069 1.645.069 4.85s-.012 3.584-.07 4.85c-.148 3.225-1.664 4.771-4.919 4.919-1.266.058-1.644.07-4.85.07s-3.584-.012-4.85-.07c-3.252-.148-4.771-1.691-4.919-4.919-.058-1.265-.069-1.645-.069-4.85s.012-3.584.07-4.85c.148-3.225 1.664-4.771 4.919-4.919C8.356 2.175 8.741 2.163 12 2.163m0-2.163c-3.259 0-3.667.014-4.947.072C2.695.272.272 2.695.072 7.053.014 8.333 0 8.741 0 12s.014 3.667.072 4.947c.2 4.358 2.618 6.78 6.98 6.98 1.281.058 1.689.072 4.948.072s3.667-.014 4.947-.072c4.358-.2 6.78-2.618 6.98-6.98.059-1.281.073-1.689.073-4.948s-.014-3.667-.072-4.947C21.728 2.695 19.305.272 14.947.072 13.667.014 13.259 0 12 0zm0 5.838a6.162 6.162 0 1 0 0 12.324 6.162 6.162 0 0 0 0-12.324zM12 16a4 4 0 1 1 0-8 4 4 0 0 1 0 8zm6.406-11.845a1.44 1.44 0 1 0 0 2.88 1.44 1.44 0 0 0 0-2.88z"></path>
    </svg>
);


// --- Sub-components ---

const ChatBubble = ({ message }) => {
    const isUser = message.sender === 'user';
    const containerClasses = isUser ? 'self-end flex-row-reverse' : 'self-start flex-row';
    const bubbleColorClass = isUser
        ? 'bg-gradient-to-br from-sky-400 to-cyan-400 text-white'
        : 'bg-white text-gray-700';
    const formatText = (text) => text.replace(/\n/g, '<br />');

    return (
        <div className={`flex items-end gap-3 max-w-lg lg:max-w-xl animate-popIn ${containerClasses}`}>
            {isUser ? <UserIcon /> : <BotIcon />}
            <div className={`rounded-2xl px-4 py-3 shadow-md ${bubbleColorClass}`}>
                <p className="text-base" dangerouslySetInnerHTML={{ __html: formatText(message.text) }} />
            </div>
        </div>
    );
};

const Header = () => (
    <div className="text-center p-6 animate-fadeIn">
        <h1 className="text-4xl font-bold text-gray-800">Tiffany's AI Assistant Demo</h1>
        <p className="text-pink-600/90 mt-1 font-semibold">Your Friendly Photoshoot Planner</p>
    </div>
);

// --- Main Application Component ---
export default function App() {
    const [messages, setMessages] = useState([]);
    const [inputValue, setInputValue] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [chatId, setChatId] = useState(null);
    const eventSourceRef = useRef(null);
    const chatContainerRef = useRef(null);

    // Load or create a session on initial mount
    useEffect(() => {
        try {
            const savedChatId = localStorage.getItem('tiffany-chat-id');
            const savedMessages = localStorage.getItem('tiffany-chat-messages');
            if (savedChatId && savedMessages) {
                setChatId(savedChatId);
                setMessages(JSON.parse(savedMessages));
            } else {
                const newChatId = crypto.randomUUID();
                setChatId(newChatId);
                setMessages([{ id: Date.now(), text: "Hello! How can I help you book a session with Tiffany?", sender: 'ai' }]);
            }
        } catch (error) {
            console.error("Failed to access localStorage:", error);
            const newChatId = crypto.randomUUID();
            setChatId(newChatId);
        }
    }, []);

    // Save session to localStorage whenever it changes
    useEffect(() => {
        try {
            if (chatId && messages.length > 0) {
                localStorage.setItem('tiffany-chat-id', chatId);
                localStorage.setItem('tiffany-chat-messages', JSON.stringify(messages));
            }
        } catch (error) {
            console.error("Failed to save to localStorage:", error);
        }
    }, [messages, chatId]);

    // Auto-scroll to the latest message
    useEffect(() => {
        if (chatContainerRef.current) {
            chatContainerRef.current.scrollTop = chatContainerRef.current.scrollHeight;
        }
    }, [messages]);

    // Close SSE connection on component unmount
    useEffect(() => {
        return () => {
            if (eventSourceRef.current) eventSourceRef.current.close();
        };
    }, []);

    const handleSendMessage = (e) => {
        e.preventDefault();
        if (!inputValue.trim() || isLoading) return;

        const userMessage = { id: Date.now(), text: inputValue, sender: 'user' };
        setMessages(prev => [...prev, userMessage]);
        setInputValue('');
        setIsLoading(true);

        if (eventSourceRef.current) eventSourceRef.current.close();

        const params = new URLSearchParams();
        params.append('message', inputValue);
        if (chatId) params.append('chatId', chatId);

        const url = `${API_BASE_URL}${API_ENDPOINT}?${params.toString()}`;

        const newEventSource = new EventSource(url);
        eventSourceRef.current = newEventSource;

        let aiMessageId = null;

        // 【MODIFIED】: This is the corrected and simplified logic that reliably handles streaming.
        newEventSource.onmessage = (event) => {
            const chunk = event.data;
            setIsLoading(false);

            if (aiMessageId === null) {
                // First chunk of the AI's response.
                aiMessageId = Date.now();
                setMessages(prev => [...prev, { id: aiMessageId, text: chunk, sender: 'ai' }]);
            } else {
                // Subsequent chunks. Append with a leading space.
                setMessages(prev => prev.map(msg => {
                    if (msg.id === aiMessageId) {
                        // Always add a space before the new chunk to prevent words from merging.
                        return { ...msg, text: msg.text + " " + chunk };
                    }
                    return msg;
                }));
            }
        };

        newEventSource.onerror = (err) => {
            setIsLoading(false);
            newEventSource.close();
            eventSourceRef.current = null;
        };
    };

    return (
        <div className="h-screen w-screen bg-gradient-to-br from-rose-100 via-orange-100 to-purple-100 font-sans flex flex-col overflow-hidden">
            <CustomStyles />
            <div className="absolute top-0 left-0 w-full h-full overflow-hidden z-0">
                <div className="absolute -top-40 -left-40 w-96 h-96 bg-purple-200 rounded-full mix-blend-multiply filter blur-xl opacity-70 animate-blob"></div>
                <div className="absolute -bottom-40 -right-20 w-96 h-96 bg-yellow-200 rounded-full mix-blend-multiply filter blur-xl opacity-70 animate-blob animation-delay-2000"></div>
                <div className="absolute -bottom-20 left-20 w-80 h-80 bg-pink-200 rounded-full mix-blend-multiply filter blur-xl opacity-70 animate-blob animation-delay-4000"></div>
            </div>

            <div className="relative z-10 flex flex-col items-center w-full h-full p-4">
                <Header />

                <div className="w-full max-w-3xl flex-1 flex flex-col min-h-0">
                    <div className="grid grid-cols-3 gap-4 mb-4 px-2">
                        <FeatureCard icon={<SimpleCalendarIcon className="w-8 h-8 mx-auto" />} title="Check Dates" description="Ask for any date or time." delay={100} />
                        <FeatureCard icon={<SimplePencilIcon className="w-8 h-8 mx-auto" />} title="Book a Slot" description="Book, reschedule, or cancel." delay={200} />
                        <FeatureCard icon={<SimpleQuestionIcon className="w-8 h-8 mx-auto" />} title="Ask Anything" description="Pricing, packages, and more." delay={300} />
                    </div>

                    <div className="w-full h-full flex flex-col bg-white/50 rounded-2xl shadow-2xl backdrop-blur-xl border border-white/30 overflow-hidden">
                        <main ref={chatContainerRef} className="flex-1 p-6 space-y-5 overflow-y-auto flex flex-col">
                            {messages.map((msg) => <ChatBubble key={msg.id} message={msg} />)}

                            {isLoading && (
                                <div className="flex items-end gap-3 max-w-xl self-start animate-popIn">
                                    <BotIcon />
                                    <div className="rounded-2xl px-4 py-3 bg-white shadow-md">
                                        <div className="flex items-center space-x-2">
                                            <div className="w-2.5 h-2.5 bg-gray-400 rounded-full animate-pulse delay-0"></div>
                                            <div className="w-2.5 h-2.5 bg-gray-400 rounded-full animate-pulse delay-200"></div>
                                            <div className="w-2.5 h-2.5 bg-gray-400 rounded-full animate-pulse delay-400"></div>
                                        </div>
                                    </div>
                                </div>
                            )}
                        </main>

                        <footer className="p-4 bg-white/30 backdrop-blur-sm border-t border-white/30">
                            <form onSubmit={handleSendMessage} className="flex items-center gap-3">
                                <input
                                    type="text"
                                    value={inputValue}
                                    onChange={(e) => setInputValue(e.target.value)}
                                    placeholder="Chat with Tiffany's AI assistant..."
                                    className="flex-1 w-full bg-white/50 text-gray-800 placeholder-gray-500/80 px-4 py-3 rounded-full border border-gray-200 focus:outline-none focus:ring-2 focus:ring-pink-400 transition-all"
                                    disabled={isLoading}
                                />
                                <button
                                    type="submit"
                                    disabled={isLoading || !inputValue.trim()}
                                    className="p-3 rounded-full bg-gradient-to-br from-pink-500 to-orange-400 text-white disabled:opacity-50 disabled:cursor-not-allowed hover:scale-110 transition-transform shadow-lg"
                                >
                                    <SendIcon className="w-6 h-6" />
                                </button>
                            </form>
                        </footer>
                    </div>

                    <div className="text-center text-xs text-gray-500/90 p-3">
                        Please note that all bookings are considered tentative until Tiffany personally reaches out to confirm the details. This AI tool is designed for preliminary consultations and scheduling.
                    </div>
                </div>
                <footer className="flex justify-center items-center gap-4 text-gray-500/80 text-xs py-2">
                    <span>&copy; {new Date().getFullYear()} Tiffanyiong Photography</span>
                    <a href="https://www.instagram.com/astralika.t" target="_blank" rel="noopener noreferrer" className="hover:text-pink-500 transition-colors">
                        <InstagramIcon className="w-5 h-5" />
                    </a>
                </footer>
            </div>
        </div>
    );
}
