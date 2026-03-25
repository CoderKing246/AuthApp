import React, { useState } from "react";
import { motion } from "motion/react";


const Home = () => {

  return (
    <div className="flex flex-col items-center justify-center bg-gray-890">
      <motion.div initial={{ opacity: 0, y: -50 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.8 }}>
        <div className="h-20" />
        <div className="text-center ">
          <h1 className="text-6xl font-bold text-white tracking-tight">Secure.Fast.Future</h1>
          <p className="max-w-md mx-auto text-lg font-medium tracking-wide uppercase text-cyan-600 dark:text-cyan-400 opacity-90">
            Next-Generation Identity & Access Management
          </p>

          <div className="mt-3 h-2 w-125 bg-gradient-to-r from-cyan-500 to-purple-500 mx-auto rounded-full" />
        </div>


      </motion.div>
    </div >

  );
};

export default Home;