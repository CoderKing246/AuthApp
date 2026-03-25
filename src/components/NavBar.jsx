import React, { useEffect, useState } from "react";
import { NavLink } from "react-router-dom";
import { IoSunnyOutline, IoMoonOutline } from "react-icons/io5";

function Navbar() {
    const [isDark, setIsDark] = useState(true);

    useEffect(() => {
        // Set initial theme
        document.body.classList.add(isDark ? "dark" : "light");
    }, []);

    const changeTheme = () => {
        document.body.classList.toggle("dark");
        document.body.classList.toggle("light");
        setIsDark(!isDark);
    };

    const navBg = isDark ? "bg-gray-900 border-gray-700" : "bg-gray-200 border-gray-500";
    const textColor = isDark ? "text-white" : "text-gray-900";
    const hoverColor = isDark ? "hover:text-cyan-300" : "hover:text-blue-600";

    return (
        <nav
            className={`${navBg} border rounded-xl shadow-lg py-4 md:py-0 flex md:flex-row flex-col md:h-16 justify-around items-center mx-6 md:mx-20 px-6 transition-colors duration-300`}
        >
            {/* Brand */}
            <div className={`font-semibold flex items-center gap-2 ${textColor}`}>
                <span className="inline-block text-center h-6 w-6 rounded-md bg-gradient-to-r from-cyan-400 to-purple-500 animate-pulse">
                    A
                </span>
                <span className="text-base tracking-tight">Auth App</span>
            </div>

            {/* Navigation Links */}
            <div className="flex gap-4 items-center mt-3 md:mt-0">
                <button
                    onClick={changeTheme}
                    className={`${textColor} text-xl p-1 rounded transition-colors duration-300`}
                >
                    {isDark ? <IoSunnyOutline /> : <IoMoonOutline />}
                </button>

                <NavLink to="/" className={`${textColor} ${hoverColor} transition-colors`}>
                    Home
                </NavLink>
                <NavLink
                    to="/login"
                    className={`px-3 py-1 ${textColor} border rounded border-gray-500 hover:border-cyan-400 ${hoverColor} transition-colors`}
                >
                    Login
                </NavLink>
                <NavLink
                    to="/signup"
                    className={`px-3 py-1 ${textColor} border rounded border-gray-500 hover:border-cyan-400 ${hoverColor} transition-colors`}
                >
                    Signup
                </NavLink>
            </div>
        </nav>
    );
}

export default Navbar;