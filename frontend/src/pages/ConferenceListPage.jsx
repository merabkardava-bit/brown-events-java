import { useState, useEffect, useRef } from 'react'
import { Link } from 'react-router-dom'
import { getConferences, createConference } from '../api'
import ConferenceCard from '../components/ConferenceCard'

const EMPTY_FORM = { title: '', description: '', location: '', startDate: '', endDate: '', status: 'UPCOMING' }

export default function ConferenceListPage() {
  const [conferences, setConferences] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  // Pagination state
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  // refreshKey forces a re-fetch even when page is already 0 (e.g. after creating a conference)
  const [refreshKey, setRefreshKey] = useState(0)

  // Filter state — rawSearch is the live input value; search is the debounced value sent to the API
  const [rawSearch, setRawSearch] = useState('')
  const [search, setSearch] = useState('')
  const [fromDate, setFromDate] = useState('')
  const [toDate, setToDate] = useState('')
  const [statusFilter, setStatusFilter] = useState('')
  const [dateRangeError, setDateRangeError] = useState(null)

  const [showModal, setShowModal] = useState(false)
  const [form, setForm] = useState(EMPTY_FORM)
  const [formError, setFormError] = useState(null)
  const [submitting, setSubmitting] = useState(false)

  // CR-007: Debounce rawSearch → search (300 ms) to avoid a request on every keystroke
  useEffect(() => {
    const timer = setTimeout(() => setSearch(rawSearch), 300)
    return () => clearTimeout(timer)
  }, [rawSearch])

  // CR-005: Track filter version so the fetch effect can detect a mid-transition run
  // where the filter changed but page has not yet settled to 0.
  const filterVersion = useRef(0)
  const lastFetchedFilterVersion = useRef(0)

  // Reset to page 0 when any filter changes; bump filterVersion before setPage so
  // the fetch effect sees the increment in the same flush.
  useEffect(() => {
    filterVersion.current += 1
    setPage(0)
  }, [search, fromDate, toDate, statusFilter])

  useEffect(() => {
    // CR-005: A filter just changed and page has not settled to 0 yet — skip this
    // stale run; the effect will re-fire once page reaches 0.
    if (page !== 0 && filterVersion.current !== lastFetchedFilterVersion.current) {
      return
    }
    lastFetchedFilterVersion.current = filterVersion.current

    // CR-008: Don't fetch while the date range is invalid; error is shown inline.
    if (fromDate && toDate && fromDate > toDate) {
      setLoading(false)
      return
    }

    // CR-006: Create an AbortController so in-flight requests are cancelled when
    // the effect re-runs, preventing stale responses from overwriting fresh ones.
    const controller = new AbortController()
    setLoading(true)
    setError(null)
    getConferences(page, 12, search, fromDate, toDate, statusFilter, controller.signal)
      .then(data => {
        setConferences(Array.isArray(data.data) ? data.data : [])
        setTotalPages(data.totalPages ?? 1)
        setTotalElements(data.totalElements ?? 0)
        setLoading(false)
      })
      .catch(err => {
        if (err.name !== 'AbortError') {
          setError(err.message || 'Failed to load conferences')
          setLoading(false)
        }
      })
    return () => controller.abort()
  }, [page, refreshKey, search, fromDate, toDate, statusFilter])

  // CR-008: Validate date range inline so users see immediate feedback
  function handleFromDateChange(e) {
    const val = e.target.value
    setFromDate(val)
    if (val && toDate && val > toDate) {
      setDateRangeError('"From" date must not be after "To" date.')
    } else {
      setDateRangeError(null)
    }
  }

  function handleToDateChange(e) {
    const val = e.target.value
    setToDate(val)
    if (fromDate && val && fromDate > val) {
      setDateRangeError('"From" date must not be after "To" date.')
    } else {
      setDateRangeError(null)
    }
  }

  function handleFormChange(e) {
    const { name, value } = e.target
    setForm(prev => ({ ...prev, [name]: value }))
  }

  function handleOpenModal() {
    setForm(EMPTY_FORM)
    setFormError(null)
    setShowModal(true)
  }

  function handleCloseModal() {
    setShowModal(false)
    setFormError(null)
  }

  function handleSubmit(e) {
    e.preventDefault()
    if (!form.title.trim()) {
      setFormError('Title is required')
      return
    }
    setSubmitting(true)
    setFormError(null)
    createConference(form)
      .then(() => {
        // Go back to page 0 and reload — new conference appears at the top
        setPage(0)
        setRefreshKey(k => k + 1)
        setShowModal(false)
        setSubmitting(false)
      })
      .catch(err => {
        setFormError(err.message || 'Failed to create conference')
        setSubmitting(false)
      })
  }

  if (loading) {
    return (
      <div>
        <nav className="navbar">
          <Link to="/" className="navbar__brand">BrownEvents</Link>
          <Link to="/" className="navbar__link">Conferences</Link>
        </nav>
        <div className="loading-container">
          <div className="spinner" />
          <p>Loading conferences...</p>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div>
        <nav className="navbar">
          <Link to="/" className="navbar__brand">BrownEvents</Link>
        </nav>
        <div className="page">
          <div className="error-container">
            <h3>Failed to load conferences</h3>
            <p>{error}</p>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div>
      <nav className="navbar">
        <Link to="/" className="navbar__brand">BrownEvents</Link>
        <Link to="/" className="navbar__link">Conferences</Link>
      </nav>

      <div className="page">
        <div className="page-header">
          <h1 className="page__title" style={{ marginBottom: 0 }}>Conferences</h1>
          <button className="btn btn--primary" onClick={handleOpenModal}>New Conference</button>
        </div>
        <p className="page__subtitle">Browse and register for upcoming technical conferences</p>

        <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap', marginBottom: dateRangeError ? '0.5rem' : '1.5rem' }}>
          <input
            type="text"
            placeholder="Search conferences..."
            value={rawSearch}
            onChange={e => setRawSearch(e.target.value)}
            style={{ flex: '1 1 200px' }}
          />
          <input
            type="date"
            value={fromDate}
            onChange={handleFromDateChange}
            title="From date"
          />
          <input
            type="date"
            value={toDate}
            onChange={handleToDateChange}
            title="To date"
          />
          <select value={statusFilter} onChange={e => setStatusFilter(e.target.value)}>
            <option value="">All statuses</option>
            <option value="UPCOMING">UPCOMING</option>
            <option value="ONGOING">ONGOING</option>
            <option value="COMPLETED">COMPLETED</option>
            <option value="CANCELLED">CANCELLED</option>
          </select>
        </div>
        {dateRangeError && (
          <p style={{ color: 'red', marginBottom: '1rem', fontSize: '0.875rem' }}>{dateRangeError}</p>
        )}

        {showModal && (
          <div className="modal-overlay" onClick={handleCloseModal}>
            <div className="modal" onClick={e => e.stopPropagation()}>
              <div className="modal__header">
                <h2 className="modal__title">New Conference</h2>
                <button className="modal__close" onClick={handleCloseModal}>&times;</button>
              </div>
              <form onSubmit={handleSubmit}>
                <div className="form-group">
                  <label>Title *</label>
                  <input name="title" value={form.title} onChange={handleFormChange} placeholder="Conference title" />
                </div>
                <div className="form-group">
                  <label>Description</label>
                  <textarea name="description" value={form.description} onChange={handleFormChange} rows={3} placeholder="Description" />
                </div>
                <div className="form-group">
                  <label>Location</label>
                  <input name="location" value={form.location} onChange={handleFormChange} placeholder="Location" />
                </div>
                <div className="form-group">
                  <label>Start Date</label>
                  <input type="date" name="startDate" value={form.startDate} onChange={handleFormChange} />
                </div>
                <div className="form-group">
                  <label>End Date</label>
                  <input type="date" name="endDate" value={form.endDate} onChange={handleFormChange} />
                </div>
                <div className="form-group">
                  <label>Status</label>
                  <select name="status" value={form.status} onChange={handleFormChange}>
                    <option value="UPCOMING">UPCOMING</option>
                    <option value="ACTIVE">ACTIVE</option>
                    <option value="COMPLETED">COMPLETED</option>
                    <option value="CANCELLED">CANCELLED</option>
                  </select>
                </div>
                {formError && <p style={{ color: 'red', marginBottom: '1rem' }}>{formError}</p>}
                <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end' }}>
                  <button type="button" className="btn btn--ghost" onClick={handleCloseModal}>Cancel</button>
                  <button type="submit" className="btn btn--primary" disabled={submitting}>
                    {submitting ? 'Creating…' : 'Create'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}

        {conferences.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '60px 0', color: '#666' }}>
            <p style={{ fontSize: '1.1rem' }}>
              {(search || fromDate || toDate || statusFilter)
                ? 'No conferences match your search.'
                : 'No conferences found.'}
            </p>
          </div>
        ) : (
          <div className="grid">
            {conferences.map(conference => (
              <ConferenceCard key={conference.id} conference={conference} />
            ))}
          </div>
        )}

        {totalPages > 1 && (
          <div className="pagination">
            <button
              className="pagination__btn"
              onClick={() => setPage(p => Math.max(0, p - 1))}
              disabled={page === 0}
            >
              ← Prev
            </button>
            <span className="pagination__info">
              Page {page + 1} of {totalPages}
              {totalElements > 0 && <> &nbsp;·&nbsp; {totalElements} total</>}
            </span>
            <button
              className="pagination__btn"
              onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
              disabled={page >= totalPages - 1}
            >
              Next →
            </button>
          </div>
        )}
      </div>
    </div>
  )
}
