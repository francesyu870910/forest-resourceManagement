import { describe, it, expect } from 'vitest'
import {
  formatDate,
  formatDateTime,
  formatNumber,
  debounce,
  throttle,
  deepClone,
  generateId,
  validateEmail,
  validatePhone,
  validateIdCard,
  isEmpty,
  uniqueArray,
  sortArray
} from '../../utils/common'

describe('Common Utils', () => {
  describe('formatDate', () => {
    it('should format date correctly', () => {
      const date = new Date('2024-01-15T10:30:00')
      expect(formatDate(date)).toBe('2024-01-15')
      expect(formatDate(date, 'YYYY-MM-DD HH:mm:ss')).toBe('2024-01-15 10:30:00')
    })

    it('should handle invalid date', () => {
      expect(formatDate(null)).toBe('')
      expect(formatDate(undefined)).toBe('')
      expect(formatDate('invalid')).toBe('')
    })
  })

  describe('formatDateTime', () => {
    it('should format datetime correctly', () => {
      const date = new Date('2024-01-15T10:30:45')
      expect(formatDateTime(date)).toBe('2024-01-15 10:30:45')
    })
  })

  describe('formatNumber', () => {
    it('should format number with decimals', () => {
      expect(formatNumber(123.456)).toBe('123.46')
      expect(formatNumber(123.456, 1)).toBe('123.5')
      expect(formatNumber(123, 0)).toBe('123')
    })

    it('should handle invalid numbers', () => {
      expect(formatNumber(null)).toBe('0')
      expect(formatNumber(undefined)).toBe('0')
      expect(formatNumber('abc')).toBe('0')
    })
  })

  describe('debounce', () => {
    it('should debounce function calls', (done) => {
      let count = 0
      const debouncedFn = debounce(() => {
        count++
      }, 100)

      debouncedFn()
      debouncedFn()
      debouncedFn()

      setTimeout(() => {
        expect(count).toBe(1)
        done()
      }, 150)
    })
  })

  describe('deepClone', () => {
    it('should deep clone objects', () => {
      const original = {
        name: 'test',
        nested: {
          value: 123,
          array: [1, 2, 3]
        }
      }

      const cloned = deepClone(original)
      cloned.nested.value = 456
      cloned.nested.array.push(4)

      expect(original.nested.value).toBe(123)
      expect(original.nested.array).toEqual([1, 2, 3])
      expect(cloned.nested.value).toBe(456)
      expect(cloned.nested.array).toEqual([1, 2, 3, 4])
    })

    it('should handle primitive values', () => {
      expect(deepClone(null)).toBe(null)
      expect(deepClone(123)).toBe(123)
      expect(deepClone('string')).toBe('string')
    })
  })

  describe('generateId', () => {
    it('should generate unique IDs', () => {
      const id1 = generateId()
      const id2 = generateId()
      expect(id1).not.toBe(id2)
      expect(id1).toMatch(/^id_/)
    })

    it('should use custom prefix', () => {
      const id = generateId('test')
      expect(id).toMatch(/^test_/)
    })
  })

  describe('validateEmail', () => {
    it('should validate email addresses', () => {
      expect(validateEmail('test@example.com')).toBe(true)
      expect(validateEmail('user.name+tag@domain.co.uk')).toBe(true)
      expect(validateEmail('invalid-email')).toBe(false)
      expect(validateEmail('test@')).toBe(false)
      expect(validateEmail('@example.com')).toBe(false)
    })
  })

  describe('validatePhone', () => {
    it('should validate Chinese phone numbers', () => {
      expect(validatePhone('13812345678')).toBe(true)
      expect(validatePhone('15987654321')).toBe(true)
      expect(validatePhone('12345678901')).toBe(false)
      expect(validatePhone('1381234567')).toBe(false)
      expect(validatePhone('138123456789')).toBe(false)
    })
  })

  describe('validateIdCard', () => {
    it('should validate Chinese ID card numbers', () => {
      expect(validateIdCard('123456789012345678')).toBe(true)
      expect(validateIdCard('12345678901234567X')).toBe(true)
      expect(validateIdCard('12345678901234567x')).toBe(true)
      expect(validateIdCard('12345678901234567')).toBe(false)
      expect(validateIdCard('1234567890123456789')).toBe(false)
    })
  })

  describe('isEmpty', () => {
    it('should check if value is empty', () => {
      expect(isEmpty(null)).toBe(true)
      expect(isEmpty(undefined)).toBe(true)
      expect(isEmpty('')).toBe(true)
      expect(isEmpty([])).toBe(true)
      expect(isEmpty({})).toBe(true)
      expect(isEmpty('test')).toBe(false)
      expect(isEmpty([1, 2, 3])).toBe(false)
      expect(isEmpty({ key: 'value' })).toBe(false)
    })
  })

  describe('uniqueArray', () => {
    it('should remove duplicates from array', () => {
      expect(uniqueArray([1, 2, 2, 3, 3, 4])).toEqual([1, 2, 3, 4])
      expect(uniqueArray(['a', 'b', 'b', 'c'])).toEqual(['a', 'b', 'c'])
    })

    it('should remove duplicates by key', () => {
      const array = [
        { id: 1, name: 'A' },
        { id: 2, name: 'B' },
        { id: 1, name: 'C' }
      ]
      const result = uniqueArray(array, 'id')
      expect(result).toHaveLength(2)
      expect(result[0].id).toBe(1)
      expect(result[1].id).toBe(2)
    })
  })

  describe('sortArray', () => {
    it('should sort array by key', () => {
      const array = [
        { name: 'Charlie', age: 30 },
        { name: 'Alice', age: 25 },
        { name: 'Bob', age: 35 }
      ]

      const sortedByName = sortArray(array, 'name')
      expect(sortedByName[0].name).toBe('Alice')
      expect(sortedByName[1].name).toBe('Bob')
      expect(sortedByName[2].name).toBe('Charlie')

      const sortedByAgeDesc = sortArray(array, 'age', 'desc')
      expect(sortedByAgeDesc[0].age).toBe(35)
      expect(sortedByAgeDesc[1].age).toBe(30)
      expect(sortedByAgeDesc[2].age).toBe(25)
    })
  })
})