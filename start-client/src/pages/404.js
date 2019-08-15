import React from 'react'
import { Link } from 'gatsby'

import { Header } from '../components/common/layout'

const NotFoundPage = () => (
  <>
    <Header />
    <div className='error'>
      <h1>
        <span className='title'>/404</span>
        NOT FOUND
      </h1>
      <p>You can navigate to the following pages:</p>
      <ul>
        <li>
          <Link to='/'>Start with Base Framework Initializr</Link>
        </li>
        <li>
          <a href='https://wiki.megvii-inc.com/x/zQ1wB'>Navigate to base-framework.io</a>
        </li>
      </ul>
    </div>
  </>
)

export default NotFoundPage
