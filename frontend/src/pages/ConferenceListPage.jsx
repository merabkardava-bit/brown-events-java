import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { getConferences, createConference } from '../api'
import ConferenceCard from '../components/ConferenceCard'

const EMPTY_FORM = { title: '', description: '', location: '', startDate: '', endDate: '', status: 'UPCOMING' }

export default function ConferenceListPage() {
  const [conferences, setConferences] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [showModal, setShowModal] = useState(false)
  const [form, setForm] = useState(EMPTY_FORM)
  const [formError, setFormError] = useState(null)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    setLoading(true)
    setError(null)
    getConferences()
      .then(data => {
        const list = data.data || data
        setConferences(Array.isArray(list) ? list : [])
        setLoading(false)
      })
      .catch(err => {
        setError(err.message || 'Failed to load conferences')
        setLoading(false)
      })
  }, [])

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
      .then(created => {
        setConferences(prev => [created, ...prev])
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
            <p style={{ fontSize: '1.1rem' }}>No conferences found.</p>
          </div>
        ) : (
          <div className="grid">
            {conferences.map(conference => (
              <ConferenceCard key={conference.id} conference={conference} />
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
