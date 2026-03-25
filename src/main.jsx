import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'
import { BrowserRouter, Routes, Route, Link, Router } from 'react-router-dom';
import RootLayout from './components/RootLayout.jsx';
import Login from './pages/Login.jsx';
import SignIn from './pages/SignIn.jsx';


createRoot(document.getElementById('root')).render(
  <BrowserRouter>
    <Routes>
      <Route path="/" element={<RootLayout />}>
        <Route index element={<App />} />
        <Route path='/login' element={<Login />} />
        <Route path='/signup' element={<SignIn />} />
        {/* <Route path='/login' element={<Login/>}/>
        <Route path='/login' element={<Login/>}/> */}
      </Route>
    </Routes>
  </BrowserRouter>,
)
