import Cookies from 'universal-cookie'
import PropTypes from 'prop-types'
import React from 'react'
import { Link } from 'gatsby'

import Logo from './Logo'

const cookies = new Cookies();

class Header extends React.Component {
    constructor(props) {
        super(props)
        this.state = {
            lang: cookies.get('lang'),
        }
    }
    render() {
        return (
            <div className='header'>
                <h1 className='logo'>
                    <Link to='/'>
                        <Logo />
                        <span className='title'>
          {this.state.lang === 'en' ? 'Base Framework':'基础库项目'} <strong>{this.state.lang === 'en' ? 'Initializr':'初始化'}</strong>
        </span>
                        <span className='description'>{this.state.lang === 'en' ? 'Bootstrap your application':'构建您的应用程序'}</span>
                    </Link>
                </h1>
                {this.props.children}
            </div>
        )
    }
}

Header.propTypes = {
  children: PropTypes.node,
}

export default Header
