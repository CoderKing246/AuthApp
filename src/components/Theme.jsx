import React, { useEffect, useState } from "react";

function ThemeToggle() {
    const [isDark, setIsDark] = useState(false);

    useEffect(() => {
        if (isDark) {
            document.documentElement.classList.add("dark");
        } else {
            document.documentElement.classList.remove("dark");
        }
    }, [isDark]);

    return (
        <button
            onClick={() => setIsDark(!isDark)}
            className="px-3 py-1 border border-gray-500 rounded hover:border-cyan-400 hover:text-cyan-400 transition-colors text-white"
        >
            {isDark ? "Light Mode" : "Dark Mode"}
        </button>
    );
}

export default ThemeToggle;