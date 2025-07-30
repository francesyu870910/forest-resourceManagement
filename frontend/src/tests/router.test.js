import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createRouter, createWebHistory } from 'vue-router'
import * as auth from '../utils/auth.js'

// Mock auth utilities
vi.mock('../utils/auth.js', () => ({
  isLoggedIn: vi.fn(),
  isAdmin: vi.fn(),
  getUserInfo: vi.fn(),
  logout: vi.fn()
}))

// Mock components
const mockComponent = { template: '<div>Mock Component</div>' }

describe('Router Guards', () => {
  let router

  beforeEach(() => {
    // Create a minimal router for testing
    router = createRouter({
      history: createWebHistory(),
      routes: [
        {
          path: '/login',
          name: 'Login',
          component: mockComponent,
          meta: { requiresGuest: true }
        },
        {
          path: '/overview',
          name: 'Overview',
          component: mockComponent,
          meta: { requiresAuth: true }
        },
        {
          path: '/users',
          name: 'UserManagement',
          component: mockComponent,
          meta: { requiresAuth: true, requiresAdmin: true }
        }
      ]
    })

    // Add the same beforeEach guard as in the actual router
    router.beforeEach((to, from, next) => {
      const loggedIn = auth.isLoggedIn()
      const userIsAdmin = auth.isAdmin()
      
      if (to.meta.requiresAuth) {
        if (!loggedIn) {
          next({
            name: 'Login',
            query: { redirect: to.fullPath }
          })
          return
        }
        
        if (to.meta.requiresAdmin && !userIsAdmin) {
          next({ name: 'Overview' })
          return
        }
      }
      
      if (to.meta.requiresGuest && loggedIn) {
        next({ name: 'Overview' })
        return
      }
      
      next()
    })
  })

  it('should redirect unauthenticated users to login page', async () => {
    auth.isLoggedIn.mockReturnValue(false)
    
    const navigationSpy = vi.fn()
    router.beforeResolve(navigationSpy)
    
    await router.push('/overview')
    
    expect(router.currentRoute.value.name).toBe('Login')
    expect(router.currentRoute.value.query.redirect).toBe('/overview')
  })

  it('should allow authenticated users to access protected routes', async () => {
    auth.isLoggedIn.mockReturnValue(true)
    auth.isAdmin.mockReturnValue(false)
    
    await router.push('/overview')
    
    expect(router.currentRoute.value.name).toBe('Overview')
  })

  it('should redirect non-admin users from admin routes', async () => {
    auth.isLoggedIn.mockReturnValue(true)
    auth.isAdmin.mockReturnValue(false)
    
    await router.push('/users')
    
    expect(router.currentRoute.value.name).toBe('Overview')
  })

  it('should allow admin users to access admin routes', async () => {
    auth.isLoggedIn.mockReturnValue(true)
    auth.isAdmin.mockReturnValue(true)
    
    await router.push('/users')
    
    expect(router.currentRoute.value.name).toBe('UserManagement')
  })

  it('should redirect authenticated users from guest-only routes', async () => {
    auth.isLoggedIn.mockReturnValue(true)
    
    await router.push('/login')
    
    expect(router.currentRoute.value.name).toBe('Overview')
  })
})