import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  TextField,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Alert,
  CircularProgress,
  Paper,
  Grid
} from '@mui/material';
import { Add, Edit, Delete, LocationOn } from '@mui/icons-material';
import client from '../../api/client';

interface Region {
  id: number;
  governorate: string;
  city: string;
  lat?: number;
  lng?: number;
}

export default function Regions() {
  const { t, i18n } = useTranslation();
  const qc = useQueryClient();
  
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingRegion, setEditingRegion] = useState<Region | null>(null);
  const [formData, setFormData] = useState({
    governorate: '',
    city: '',
    lat: '',
    lng: ''
  });
  const [error, setError] = useState('');

  const { data: regions, isLoading } = useQuery({
    queryKey: ['regions'],
    queryFn: async () => (await client.get('/public/regions')).data
  });

  const createMutation = useMutation({
    mutationFn: async (data: any) => 
      (await client.post('/admin/regions', data)).data,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['regions'] });
      handleCloseDialog();
    },
    onError: (err: any) => {
      setError(err.response?.data?.message || 'Failed to create region');
    }
  });

  const updateMutation = useMutation({
    mutationFn: async ({ id, data }: { id: number; data: any }) =>
      (await client.put(`/admin/regions/${id}`, data)).data,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['regions'] });
      handleCloseDialog();
    },
    onError: (err: any) => {
      setError(err.response?.data?.message || 'Failed to update region');
    }
  });

  const deleteMutation = useMutation({
    mutationFn: async (id: number) =>
      await client.delete(`/admin/regions/${id}`),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['regions'] });
    },
    onError: (err: any) => {
      setError(err.response?.data?.message || 'Failed to delete region');
    }
  });

  const handleOpenDialog = (region?: Region) => {
    if (region) {
      setEditingRegion(region);
      setFormData({
        governorate: region.governorate,
        city: region.city,
        lat: region.lat?.toString() || '',
        lng: region.lng?.toString() || ''
      });
    } else {
      setEditingRegion(null);
      setFormData({
        governorate: '',
        city: '',
        lat: '',
        lng: ''
      });
    }
    setError('');
    setDialogOpen(true);
  };

  const handleCloseDialog = () => {
    setDialogOpen(false);
    setEditingRegion(null);
    setFormData({
      governorate: '',
      city: '',
      lat: '',
      lng: ''
    });
    setError('');
  };

  const handleChange = (field: string) => (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData(prev => ({ ...prev, [field]: e.target.value }));
  };

  const handleSubmit = () => {
    if (!formData.governorate.trim() || !formData.city.trim()) {
      setError(i18n.language === 'ar' ? 'المحافظة والمدينة مطلوبة' : 'Governorate and city are required');
      return;
    }

    const submitData = {
      governorate: formData.governorate,
      city: formData.city,
      lat: formData.lat ? parseFloat(formData.lat) : null,
      lng: formData.lng ? parseFloat(formData.lng) : null
    };

    if (editingRegion) {
      updateMutation.mutate({ id: editingRegion.id, data: submitData });
    } else {
      createMutation.mutate(submitData);
    }
  };

  const handleDelete = (id: number) => {
    if (window.confirm(i18n.language === 'ar' ? 'هل أنت متأكد؟' : 'Are you sure?')) {
      deleteMutation.mutate(id);
    }
  };

  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      <Card>
        <CardContent>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
            <Typography variant="h5" sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <LocationOn />
              {i18n.language === 'ar' ? 'إدارة المناطق' : 'Manage Regions'}
            </Typography>
            <Button
              variant="contained"
              startIcon={<Add />}
              onClick={() => handleOpenDialog()}
            >
              {i18n.language === 'ar' ? 'إضافة منطقة' : 'Add Region'}
            </Button>
          </Box>

          {error && (
            <Alert severity="error" onClose={() => setError('')} sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}

          <TableContainer component={Paper} variant="outlined">
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell><strong>ID</strong></TableCell>
                  <TableCell><strong>{i18n.language === 'ar' ? 'المحافظة' : 'Governorate'}</strong></TableCell>
                  <TableCell><strong>{i18n.language === 'ar' ? 'المدينة' : 'City'}</strong></TableCell>
                  <TableCell><strong>{i18n.language === 'ar' ? 'الإحداثيات' : 'Coordinates'}</strong></TableCell>
                  <TableCell align="right"><strong>{i18n.language === 'ar' ? 'الإجراءات' : 'Actions'}</strong></TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {(regions || []).length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={5} align="center">
                      <Typography color="text.secondary">
                        {i18n.language === 'ar' ? 'لا توجد مناطق' : 'No regions yet'}
                      </Typography>
                    </TableCell>
                  </TableRow>
                ) : (
                  (regions || []).map((region: Region) => (
                    <TableRow key={region.id}>
                      <TableCell>{region.id}</TableCell>
                      <TableCell>{region.governorate}</TableCell>
                      <TableCell>{region.city}</TableCell>
                      <TableCell>
                        {region.lat && region.lng 
                          ? `${region.lat.toFixed(4)}, ${region.lng.toFixed(4)}`
                          : '-'}
                      </TableCell>
                      <TableCell align="right">
                        <IconButton
                          color="primary"
                          onClick={() => handleOpenDialog(region)}
                          size="small"
                        >
                          <Edit />
                        </IconButton>
                        <IconButton
                          color="error"
                          onClick={() => handleDelete(region.id)}
                          size="small"
                        >
                          <Delete />
                        </IconButton>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </CardContent>
      </Card>

      {/* Add/Edit Dialog */}
      <Dialog open={dialogOpen} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle>
          {editingRegion
            ? (i18n.language === 'ar' ? 'تعديل المنطقة' : 'Edit Region')
            : (i18n.language === 'ar' ? 'إضافة منطقة' : 'Add Region')}
        </DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                fullWidth
                label={i18n.language === 'ar' ? 'المحافظة' : 'Governorate'}
                value={formData.governorate}
                onChange={handleChange('governorate')}
                required
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                fullWidth
                label={i18n.language === 'ar' ? 'المدينة' : 'City'}
                value={formData.city}
                onChange={handleChange('city')}
                required
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                fullWidth
                label={i18n.language === 'ar' ? 'خط العرض' : 'Latitude'}
                value={formData.lat}
                onChange={handleChange('lat')}
                type="number"
                inputProps={{ step: 'any' }}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                fullWidth
                label={i18n.language === 'ar' ? 'خط الطول' : 'Longitude'}
                value={formData.lng}
                onChange={handleChange('lng')}
                type="number"
                inputProps={{ step: 'any' }}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>
            {i18n.language === 'ar' ? 'إلغاء' : 'Cancel'}
          </Button>
          <Button
            variant="contained"
            onClick={handleSubmit}
            disabled={createMutation.isPending || updateMutation.isPending}
          >
            {createMutation.isPending || updateMutation.isPending ? (
              <CircularProgress size={20} />
            ) : editingRegion ? (
              i18n.language === 'ar' ? 'تحديث' : 'Update'
            ) : (
              i18n.language === 'ar' ? 'إضافة' : 'Add'
            )}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
