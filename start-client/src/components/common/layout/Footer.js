import React from 'react'

const Footer = () => (
  <footer className='footer'>
    <div className='footer-container'>
      © 2019.8-{new Date().getFullYear()} Pivotal Software
      <br />
      start.base-framework.io is powered by
      <br />
      <span>
        <a
          tabIndex='-1'
          target='_blank'
          rel='noopener noreferrer'
          href='https://wiki.megvii-inc.com/x/zQ1wB'
        >
          Base Framework Initializr
        </a>
      </span>
      <span>
        {` `}and{` `}
      </span>
      <span>
        <a
          tabIndex='-1'
          target='_blank'
          rel='noopener noreferrer'
          href='https://run.pivotal.io/'
        >
          Pivotal Web Services
        </a>
      </span>
    </div>
  </footer>
)

export default Footer
