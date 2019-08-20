import Cookies from 'universal-cookie'
import React from 'react'
import get from 'lodash.get'

import { IconCaretDown, IconGithub, IconSpring, IconTwitter } from '../icons'

const cookies = new Cookies();

class QuickLinks extends React.Component {
  constructor(props) {
    super(props)

    this.setWrapperRef = this.setWrapperRef.bind(this)
    this.handleClickOutside = this.handleClickOutside.bind(this)
    const min = 1;
    const max = 100;
    const rand = min + Math.random() * (max - min);
    this.state = {
      help: false,
      pageLang: false,
      lang: cookies.get('lang'),
      random: rand,
    }
  }

  componentDidMount() {
    document.addEventListener('mousedown', this.handleClickOutside)
  }

  componentWillUnmount() {
    document.removeEventListener('mousedown', this.handleClickOutside)
  }

  setWrapperRef(node) {
    this.wrapperRef = node
  }

  handleClickOutside(event) {
    if (this.wrapperRef && !this.wrapperRef.contains(event.target)) {
      this.setState({ help: false })
      this.setState({ pageLang: false })
    }
  }

  render() {
    return (
      <ul className='quick-links'>
        {/*<li>
          <a
            href='https://github.com/spring-io/start.spring.io'
            rel='noreferrer noopener'
            target='_blank'
            tabIndex='-1'
          >
            <IconGithub />
            Github
          </a>
        </li>
        <li>
          <a
            href='https://twitter.com/springboot'
            rel='noreferrer noopener'
            target='_blank'
            tabIndex='-1'
          >
            <IconTwitter />
            Twitter
          </a>
        </li>*/}
        <li>
          <a
              href='/'
              className='dropdown'
              tabIndex='-1'
              onClick={e => {
                e.preventDefault()
                this.setState({ pageLang: !this.state.pageLang })
              }}
          >{this.state.lang === 'en' ? 'language':'语言'}
            <IconCaretDown className='caret' />
          </a>
          {this.state.pageLang && (
              <ul className='dropdown-menu' ref={this.setWrapperRef}>
                <li>
                  <a
                      id='ql-lang-projects'
                      target=''
                      rel='noopener noreferrer'
                      href={'/?lang=zh&random=' + get(this.state, 'random')}
                      tabIndex='-1'
                      onClick={() => {
                        this.setState({ pageLang: false })
                      }}
                  >
                    中文
                  </a>
                </li>
                <li>
                  <a
                      id='ql-lang-projects'
                      target=''
                      rel='noopener noreferrer'
                      href={'/?lang=en&random=' + get(this.state, 'random')}
                      tabIndex='-1'
                      onClick={() => {
                        this.setState({ pageLang: false })
                      }}
                  >
                    English
                  </a>
                </li>
              </ul>
          )}
        </li>
        <li>
          <a
            href='/'
            className='dropdown'
            tabIndex='-1'
            onClick={e => {
              e.preventDefault()
              this.setState({ help: !this.state.help })
            }}
          >
            <IconSpring />
            {this.state.lang === 'en' ? 'Help':'帮助文档'}
            <IconCaretDown className='caret' />
          </a>
          {this.state.help && (
            <ul className='dropdown-menu' ref={this.setWrapperRef}>
              <li>
                <a
                    id='ql-help-projects'
                    target='_blank'
                    rel='noopener noreferrer'
                    href='https://wiki.megvii-inc.com/x/zQ1wB'
                    tabIndex='-1'
                    onClick={() => {
                      this.setState({ help: false })
                    }}
                >
                  {this.state.lang === 'en' ? 'Base Framework Projects':'基础库项目'}
                </a>
              </li>
              <li>
                <a
                  id='ql-help-projects'
                  target='_blank'
                  rel='noopener noreferrer'
                  href='https://spring.io/projects'
                  tabIndex='-1'
                  onClick={() => {
                    this.setState({ help: false })
                  }}
                >
                  {this.state.lang === 'en' ? 'Spring Projects':'Spring项目'}
                </a>
              </li>
              <li>
                <a
                  id='ql-help-guides'
                  target='_blank'
                  rel='noopener noreferrer'
                  tabIndex='-1'
                  href='https://spring.io/guides'
                  onClick={() => {
                    this.setState({ help: false })
                  }}
                >
                  {this.state.lang === 'en' ? 'Spring Guides':'Spring指南'}
                </a>
              </li>
              <li>
                <a
                  id='ql-help-spring-blog'
                  target='_blank'
                  rel='noopener noreferrer'
                  tabIndex='-1'
                  href='https://spring.io/blog'
                  onClick={() => {
                    this.setState({ help: false })
                  }}
                >
                  {this.state.lang === 'en' ? 'What\'s New With Spring':'Spring新特性'}
                </a>
              </li>
              <li>
                <a
                  id='ql-help-migration'
                  target='_blank'
                  rel='noopener noreferrer'
                  tabIndex='-1'
                  href='https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.0-Migration-Guide'
                  onClick={() => {
                    this.setState({ help: false })
                  }}
                >
                  {this.state.lang === 'en' ? 'Migrate from 1.5 => 2.0':'1.5 => 2.0迁移指南'}
                </a>
              </li>
            </ul>
          )}
        </li>
      </ul>
    )
  }
}

export default QuickLinks
