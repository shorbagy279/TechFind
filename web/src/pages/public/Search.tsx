import React, { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { GridLegacy as Grid } from '@mui/material';

import {
  Box,
  Card,
  CardContent,
  Typography,
  TextField,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Button,
  Chip,
  Rating,
  Avatar,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Slider,
  Switch,
  FormControlLabel,
  CircularProgress,
  Alert,
  Fab,
  Badge
} from '@mui/material';
import {
  Search as SearchIcon,
  LocationOn,
  Phone,
  WhatsApp,
  Message,
  Star,
  FilterList,
  MyLocation,
  Clear,
  Sort
} from '@mui/icons-material';
import client from '../../api/client';

interface Technician {
  id: number;
  fullName: string;
  phone: string;
  email: string;
  summary: string;
  description: string;
  profilePhotoUrl?: string;
  averageRating: number;
  totalReviews: number;
  baseServiceFee?: number;
  currency: string;
  experienceYears: number;
  region: {
    governorate: string;
    city: string;
  };
  fields: Array<{ id: number; name: string }>;
  availabilityStatus: 'AVAILABLE' | 'BUSY' | 'OFFLINE';
  allowDirectCalls: boolean;
  allowWhatsApp: boolean;
  allowInAppMessages: boolean;
  isEmergencyService: boolean;
  distance?: number;
}

interface SearchFilters {
  governorate: string;
  city: string;
  field: string;
  minRating: number;
  maxPrice: number;
  maxDistance: number;
  availableNow: boolean;
  emergencyService: boolean;
  sortBy: string;
}

export default function EnhancedSearch() {
  const { t, i18n } = useTranslation();
  const [filters, setFilters] = useState<SearchFilters>({
    governorate: '',
    city: '',
    field: '',
    minRating: 0,
    maxPrice: 1000,
    maxDistance: 50,
    availableNow: false,
    emergencyService: false,
    sortBy: 'distance'
  });
  
  const [userLocation, setUserLocation] = useState<{ lat: number; lng: number } | null>(null);
  const [showFilters, setShowFilters] = useState(false);
  const [selectedTechnician, setSelectedTechnician] = useState<Technician | null>(null);
  const [contactDialog, setContactDialog] = useState(false);
  const [locationPermission, setLocationPermission] = useState<'granted' | 'denied' | 'pending'>('pending');

  // Get user's current location
  useEffect(() => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          setUserLocation({
            lat: position.coords.latitude,
            lng: position.coords.longitude
          });
          setLocationPermission('granted');
        },
        (error) => {
          console.error('Location error:', error);
          setLocationPermission('denied');
        }
      );
    }
  }, []);

  // Fetch data
  const { data: fields } = useQuery({
    queryKey: ['fields'],
    queryFn: async () => (await client.get('/public/fields')).data
  });

  const { data: regions } = useQuery({
    queryKey: ['regions'],
    queryFn: async () => (await client.get('/public/regions')).data
  });

  const { data: searchResult, isLoading, refetch } = useQuery({
    queryKey: ['search', filters, userLocation],
    queryFn: async () => {
      const params: any = {
        ...filters,
        userLat: userLocation?.lat,
        userLng: userLocation?.lng
      };
      
      // Remove empty values
      Object.keys(params).forEach(key => {
        if (params[key] === '' || params[key] === 0) {
          delete params[key];
        }
      });
      
      return (await client.get('/public/technicians/search', { params })).data;
    },
    enabled: true
  });

  const handleFilterChange = (key: keyof SearchFilters, value: any) => {
    setFilters(prev => ({ ...prev, [key]: value }));
  };

  const requestLocation = () => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          setUserLocation({
            lat: position.coords.latitude,
            lng: position.coords.longitude
          });
          setLocationPermission('granted');
        },
        (error) => {
          setLocationPermission('denied');
        }
      );
    }
  };

  const clearFilters = () => {
    setFilters({
      governorate: '',
      city: '',
      field: '',
      minRating: 0,
      maxPrice: 1000,
      maxDistance: 50,
      availableNow: false,
      emergencyService: false,
      sortBy: 'distance'
    });
  };

  const handleContact = async (technician: Technician, method: string) => {
    try {
      const response = await client.post(`/user/technicians/${technician.id}/contact`, {
        contactMethod: method
      });
      
      if (response.data.allowed) {
        switch (method) {
          case 'call':
            window.open(`tel:${response.data.phoneNumber}`);
            break;
          case 'whatsapp':
            window.open(response.data.whatsappLink, '_blank');
            break;
          case 'message':
            // Navigate to message thread
            console.log('Message thread:', response.data.messageThreadId);
            break;
        }
      } else {
        alert(response.data.message || 'Contact method not available');
      }
    } catch (error) {
      console.error('Contact error:', error);
    }
  };

  const getAvailabilityColor = (status: string) => {
    switch (status) {
      case 'AVAILABLE': return 'success';
      case 'BUSY': return 'warning';
      case 'OFFLINE': return 'error';
      default: return 'default';
    }
  };

  const getDistanceText = (distance?: number) => {
    if (distance === undefined) return '';
    if (distance < 1) return `${Math.round(distance * 1000)}m`;
    return `${distance.toFixed(1)}km`;
  };

  return (
    <Box sx={{ p: 3 }}>
      {/* Header with location status */}
      <Box sx={{ mb: 3, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Typography variant="h4" component="h1">
          {t('search')}
        </Typography>
        
        {locationPermission === 'denied' && (
          <Alert severity="warning" action={
            <Button color="inherit" size="small" onClick={requestLocation}>
              Enable Location
            </Button>
          }>
            Location access needed for distance-based search
          </Alert>
        )}
        
        {locationPermission === 'granted' && (
          <Chip 
            icon={<LocationOn />}
            label="Location enabled"
            color="success"
            variant="outlined"
          />
        )}
      </Box>

      {/* Search Bar and Quick Filters */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Grid container spacing={2} alignItems="center">
            <Grid item xs={12} sm={6} md={3}>
              <TextField
                fullWidth
                label={t('governorate')}
                value={filters.governorate}
                onChange={(e) => handleFilterChange('governorate', e.target.value)}
                size="small"
              />
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <TextField
                fullWidth
                label={t('city')}
                value={filters.city}
                onChange={(e) => handleFilterChange('city', e.target.value)}
                size="small"
              />
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <FormControl fullWidth size="small">
                <InputLabel>{t('field')}</InputLabel>
                <Select
                  value={filters.field}
                  onChange={(e) => handleFilterChange('field', e.target.value)}
                  label={t('field')}
                >
                  <MenuItem value="">All Fields</MenuItem>
                  {(fields || []).map((field: any) => (
                    <MenuItem key={field.id} value={field.name}>
                      {field.name}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <Box sx={{ display: 'flex', gap: 1 }}>
                <Button
                  variant="contained"
                  startIcon={<SearchIcon />}
                  onClick={() => refetch()}
                  disabled={isLoading}
                  fullWidth
                >
                  {isLoading ? <CircularProgress size={20} /> : t('search')}
                </Button>
                <IconButton onClick={() => setShowFilters(true)}>
                  <FilterList />
                </IconButton>
              </Box>
            </Grid>
          </Grid>
          
          {/* Quick Filter Chips */}
          <Box sx={{ mt: 2, display: 'flex', gap: 1, flexWrap: 'wrap' }}>
            <FormControlLabel
              control={
                <Switch
                  checked={filters.availableNow}
                  onChange={(e) => handleFilterChange('availableNow', e.target.checked)}
                  size="small"
                />
              }
              label="Available Now"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={filters.emergencyService}
                  onChange={(e) => handleFilterChange('emergencyService', e.target.checked)}
                  size="small"
                />
              }
              label="Emergency Service"
            />
            {userLocation && (
              <Chip
                icon={<MyLocation />}
                label={`Within ${filters.maxDistance}km`}
                onClick={() => setShowFilters(true)}
                clickable
              />
            )}
            <Chip
              icon={<Star />}
              label={`${filters.minRating}+ stars`}
              onClick={() => setShowFilters(true)}
              clickable
            />
          </Box>
        </CardContent>
      </Card>

      {/* Search Results */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h6">
          {searchResult ? `${searchResult.totalElements} technicians found` : 'Search for technicians'}
        </Typography>
        
        <FormControl size="small" sx={{ minWidth: 120 }}>
          <InputLabel>Sort by</InputLabel>
          <Select
            value={filters.sortBy}
            onChange={(e) => handleFilterChange('sortBy', e.target.value)}
            label="Sort by"
          >
            {userLocation && <MenuItem value="distance">Distance</MenuItem>}
            <MenuItem value="rating">Rating</MenuItem>
            <MenuItem value="price">Price</MenuItem>
            <MenuItem value="name">Name</MenuItem>
          </Select>
        </FormControl>
      </Box>

      {/* Results Grid */}
      <Grid container spacing={3}>
        {(searchResult?.technicians || []).map((tech: Technician) => (
          <Grid item xs={12} sm={6} md={4} key={tech.id}>
            <Card 
              sx={{ 
                height: '100%', 
                cursor: 'pointer',
                '&:hover': { 
                  boxShadow: (theme) => theme.shadows[8],
                  transform: 'translateY(-2px)',
                  transition: 'all 0.3s ease'
                }
              }}
              onClick={() => setSelectedTechnician(tech)}
            >
              <CardContent>
                {/* Header with avatar and availability */}
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <Badge
                    overlap="circular"
                    anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
                    badgeContent={
                      <Box
                        sx={{
                          width: 12,
                          height: 12,
                          borderRadius: '50%',
                          bgcolor: tech.availabilityStatus === 'AVAILABLE' ? 'success.main' :
                                  tech.availabilityStatus === 'BUSY' ? 'warning.main' : 'error.main',
                          border: '2px solid white'
                        }}
                      />
                    }
                  >
                    <Avatar 
                      src={tech.profilePhotoUrl} 
                      sx={{ width: 50, height: 50 }}
                    >
                      {tech.fullName.charAt(0)}
                    </Avatar>
                  </Badge>
                  
                  <Box sx={{ ml: 2, flexGrow: 1 }}>
                    <Typography variant="h6" noWrap>
                      {tech.fullName}
                    </Typography>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <Rating value={tech.averageRating} size="small" readOnly />
                      <Typography variant="body2" color="text.secondary">
                        ({tech.totalReviews})
                      </Typography>
                    </Box>
                  </Box>
                  
                  {tech.distance !== undefined && (
                    <Chip 
                      label={getDistanceText(tech.distance)}
                      size="small"
                      color="primary"
                      variant="outlined"
                    />
                  )}
                </Box>

                {/* Location and Experience */}
                <Box sx={{ mb: 2 }}>
                  <Typography variant="body2" color="text.secondary" sx={{ display: 'flex', alignItems: 'center', mb: 0.5 }}>
                    <LocationOn fontSize="small" sx={{ mr: 0.5 }} />
                    {tech.region.governorate}, {tech.region.city}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {tech.experienceYears} years experience
                  </Typography>
                </Box>

                {/* Fields */}
                <Box sx={{ mb: 2, display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                  {tech.fields.slice(0, 3).map(field => (
                    <Chip key={field.id} label={field.name} size="small" variant="outlined" />
                  ))}
                  {tech.fields.length > 3 && (
                    <Chip label={`+${tech.fields.length - 3} more`} size="small" variant="outlined" />
                  )}
                </Box>

                {/* Summary */}
                <Typography variant="body2" sx={{ mb: 2, display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
                  {tech.summary}
                </Typography>

                {/* Price and Contact Options */}
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <Typography variant="h6" color="primary">
                    {tech.baseServiceFee ? `${tech.baseServiceFee} ${tech.currency}` : 'Negotiable'}
                  </Typography>
                  
                  <Box sx={{ display: 'flex', gap: 0.5 }}>
                    {tech.allowDirectCalls && (
                      <IconButton 
                        size="small" 
                        color="primary"
                        onClick={(e) => {
                          e.stopPropagation();
                          handleContact(tech, 'call');
                        }}
                      >
                        <Phone />
                      </IconButton>
                    )}
                    {tech.allowWhatsApp && (
                      <IconButton 
                        size="small" 
                        sx={{ color: '#25D366' }}
                        onClick={(e) => {
                          e.stopPropagation();
                          handleContact(tech, 'whatsapp');
                        }}
                      >
                        <WhatsApp />
                      </IconButton>
                    )}
                    {tech.allowInAppMessages && (
                      <IconButton 
                        size="small" 
                        color="secondary"
                        onClick={(e) => {
                          e.stopPropagation();
                          handleContact(tech, 'message');
                        }}
                      >
                        <Message />
                      </IconButton>
                    )}
                  </Box>
                </Box>

                {/* Emergency badge */}
                {tech.isEmergencyService && (
                  <Chip 
                    label="Emergency Service" 
                    color="error" 
                    size="small" 
                    sx={{ mt: 1 }}
                  />
                )}
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      {/* No Results */}
      {searchResult && searchResult.technicians.length === 0 && (
        <Box sx={{ textAlign: 'center', py: 8 }}>
          <Typography variant="h6" color="text.secondary" gutterBottom>
            {t('no_results')}
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Try adjusting your search criteria
          </Typography>
          <Button variant="outlined" onClick={clearFilters}>
            Clear Filters
          </Button>
        </Box>
      )}

      {/* Advanced Filters Dialog */}
      <Dialog open={showFilters} onClose={() => setShowFilters(false)} maxWidth="sm" fullWidth>
        <DialogTitle sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          Advanced Filters
          <IconButton onClick={clearFilters}>
            <Clear />
          </IconButton>
        </DialogTitle>
        <DialogContent>
          <Box sx={{ pt: 1 }}>
            {/* Rating Filter */}
            <Typography gutterBottom>Minimum Rating</Typography>
            <Slider
              value={filters.minRating}
              onChange={(_, value) => handleFilterChange('minRating', value)}
              min={0}
              max={5}
              step={0.5}
              marks
              valueLabelDisplay="auto"
              sx={{ mb: 3 }}
            />

            {/* Price Filter */}
            <Typography gutterBottom>Maximum Price ({(fields || [])[0]?.currency || 'EGP'})</Typography>
            <Slider
              value={filters.maxPrice}
              onChange={(_, value) => handleFilterChange('maxPrice', value)}
              min={0}
              max={2000}
              step={50}
              valueLabelDisplay="auto"
              sx={{ mb: 3 }}
            />

            {/* Distance Filter (if location available) */}
            {userLocation && (
              <>
                <Typography gutterBottom>Maximum Distance (km)</Typography>
                <Slider
                  value={filters.maxDistance}
                  onChange={(_, value) => handleFilterChange('maxDistance', value)}
                  min={1}
                  max={100}
                  step={5}
                  valueLabelDisplay="auto"
                  sx={{ mb: 3 }}
                />
              </>
            )}

            {/* Boolean Filters */}
            <FormControlLabel
              control={
                <Switch
                  checked={filters.availableNow}
                  onChange={(e) => handleFilterChange('availableNow', e.target.checked)}
                />
              }
              label="Available Now Only"
              sx={{ display: 'block', mb: 1 }}
            />
            
            <FormControlLabel
              control={
                <Switch
                  checked={filters.emergencyService}
                  onChange={(e) => handleFilterChange('emergencyService', e.target.checked)}
                />
              }
              label="Emergency Services Only"
              sx={{ display: 'block' }}
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowFilters(false)}>Cancel</Button>
          <Button onClick={() => {setShowFilters(false); refetch();}} variant="contained">
            Apply Filters
          </Button>
        </DialogActions>
      </Dialog>

      {/* Floating Action Button for Map View */}
      {userLocation && (
        <Fab
          color="primary"
          aria-label="map"
          sx={{ position: 'fixed', bottom: 16, right: 16 }}
          onClick={() => {/* Navigate to map view */}}
        >
          <LocationOn />
        </Fab>
      )}
    </Box>
  );
}