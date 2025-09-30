import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
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
  Grid,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Chip,
  Avatar
} from '@mui/material';
import { Add, Edit, Delete, Visibility, Build } from '@mui/icons-material';
import client from '../../api/client';

interface Technician {
  id: number;
  fullName: string;
  phone: string;
  email: string;
  region: {
    governorate: string;
    city: string;
  };
  fields: Array<{ id: number; name: string }>;
  averageRating: number;
  totalReviews: number;
  active: boolean;
  availabilityStatus: string;
}

export default function TechnicianList() {
  const { t, i18n } = useTranslation();
  const qc = useQueryClient();
  
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingTech, setEditingTech] = useState<Technician | null>(null);
  const [formData, setFormData] = useState({
    fullName: '',
    phone: '',
    email: '',
    summary: '',
    regionId: '',
    fieldIds: [] as number[],
    experienceYears: '',
    baseServiceFee: '',
    currency: 'EGP'
  });
  const [error, setError] = useState('');

  const { data: regions } = useQuery({
    queryKey: ['regions'],
    queryFn: async () => (await client.get('/public/regions')).data
  });

  const { data: fields } = useQuery({
    queryKey: ['fields'],
    queryFn: async () => (await client.get('/public/fields')).data
  });

  const { data: technicians, isLoading } = useQuery({
    queryKey: ['admin-techs'],
    queryFn: async () => {
      const response = await client.get('/public/technicians/search');
      return response.data.technicians || [];
    }
  });

  const createMutation = useMutation({
    mutationFn: async (data: any) => 
      (await client.post('/admin/technicians', data)).data,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['admin-techs'] });
      handleCloseDialog();
    },
    onError: (err: any) => {
      setError(err.response?.data?.message || 'Failed to create technician');
    }
  });

  const deleteMutation = useMutation({
    mutationFn: async (id: number) =>
      await client.delete(`/admin/technicians/${id}`),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['admin-techs'] });
    },
    onError: (err: any) => {
      setError(err.response?.data?.message || 'Failed to delete technician');
    }
  });

  const handleOpenDialog = (tech?: Technician) => {
    if (tech) {
      setEditingTech(tech);
      setFormData({
        fullName: tech.fullName,
        phone: tech.phone,
        email: tech.email,
        summary: '',
        regionId: '',
        fieldIds: tech.fields.map(f => f.id),
        experienceYears: '',
        baseServiceFee: '',
        currency: 'EGP'
      });
    } else {
      setEditingTech(null);
      setFormData({
        fullName: '',
        phone: '',
        email: '',
        summary: '',
        regionId: '',
        fieldIds: [],
        experienceYears: '',
        baseServiceFee: '',
        currency: 'EGP'
      });
    }
    setError('');
    setDialogOpen(true);
  };

  const handleCloseDialog = () => {
    setDialogOpen(false);
    setEditingTech(null);
    setError('');
  };

  const handleChange = (field: string) => (e: any) => {
    setFormData(prev => ({ ...prev, [field]: e.target.value }));
  };

  const handleSubmit = () => {
    if (!formData.fullName.trim()) {
      setError(i18n.language === 'ar' ? 'الاسم مطلوب' : 'Name is required');
      return;
    }

    const submitData = {
      fullName: formData.fullName,
      phone: formData.phone,
      email: formData.email,
      summary: formData.summary,
      regionId: parseInt(formData.regionId),
      fieldIds: formData.fieldIds,
      experienceYears: formData.experienceYears ? parseInt(formData.experienceYears) : 0,
      baseServiceFee: formData.baseServiceFee ? parseFloat(formData.baseServiceFee) : null,
      currency: formData.currency,
      allowDirectCalls: true,
      allowWhatsApp: true,
      allowInAppMessages: true
    };

    createMutation.mutate(submitData);
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
              <Build />
              {i18n.language === 'ar' ? 'إدارة الفنيين' : 'Manage Technicians'}
            </Typography>
            <Button
              variant="contained"
              startIcon={<Add />}
              onClick={() => handleOpenDialog()}
            >
              {i18n.language === 'ar' ? 'إضافة فني' : 'Add Technician'}
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
                  <TableCell><strong>{i18n.language === 'ar' ? 'الفني' : 'Technician'}</strong></TableCell>
                  <TableCell><strong>{i18n.language === 'ar' ? 'المنطقة' : 'Region'}</strong></TableCell>
                  <TableCell><strong>{i18n.language === 'ar' ? 'المجالات' : 'Fields'}</strong></TableCell>
                  <TableCell><strong>{i18n.language === 'ar' ? 'التقييم' : 'Rating'}</strong></TableCell>
                  <TableCell><strong>{i18n.language === 'ar' ? 'الحالة' : 'Status'}</strong></TableCell>
                  <TableCell align="right"><strong>{i18n.language === 'ar' ? 'الإجراءات' : 'Actions'}</strong></TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {(technicians || []).length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={6} align="center">
                      <Typography color="text.secondary">
                        {i18n.language === 'ar' ? 'لا يوجد فنيين' : 'No technicians yet'}
                      </Typography>
                    </TableCell>
                  </TableRow>
                ) : (
                  (technicians || []).map((tech: Technician) => (
                    <TableRow key={tech.id}>
                      <TableCell>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                          <Avatar>{tech.fullName.charAt(0)}</Avatar>
                          <Box>
                            <Typography variant="body2" sx={{ fontWeight: 'bold' }}>
                              {tech.fullName}
                            </Typography>
                            <Typography variant="caption" color="text.secondary">
                              {tech.email}
                            </Typography>
                          </Box>
                        </Box>
                      </TableCell>
                      <TableCell>
                        {tech.region?.governorate}, {tech.region?.city}
                      </TableCell>
                      <TableCell>
                        <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap' }}>
                          {tech.fields?.slice(0, 2).map(field => (
                            <Chip key={field.id} label={field.name} size="small" />
                          ))}
                          {tech.fields?.length > 2 && (
                            <Chip label={`+${tech.fields.length - 2}`} size="small" />
                          )}
                        </Box>
                      </TableCell>
                      <TableCell>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                          <Typography variant="body2">
                            {tech.averageRating?.toFixed(1) || '0.0'}
                          </Typography>
                          <Typography variant="caption" color="text.secondary">
                            ({tech.totalReviews || 0})
                          </Typography>
                        </Box>
                      </TableCell>
                      <TableCell>
                        <Chip 
                          label={tech.availabilityStatus || 'OFFLINE'}
                          size="small"
                          color={tech.availabilityStatus === 'AVAILABLE' ? 'success' : 'default'}
                        />
                      </TableCell>
                      <TableCell align="right">
                        <IconButton
                          color="primary"
                          component={Link}
                          to={`/technicians/${tech.id}`}
                          size="small"
                        >
                          <Visibility />
                        </IconButton>
                        <IconButton
                          color="primary"
                          onClick={() => handleOpenDialog(tech)}
                          size="small"
                        >
                          <Edit />
                        </IconButton>
                        <IconButton
                          color="error"
                          onClick={() => handleDelete(tech.id)}
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
      <Dialog open={dialogOpen} onClose={handleCloseDialog} maxWidth="md" fullWidth>
        <DialogTitle>
          {editingTech
            ? (i18n.language === 'ar' ? 'تعديل الفني' : 'Edit Technician')
            : (i18n.language === 'ar' ? 'إضافة فني' : 'Add Technician')}
        </DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                fullWidth
                label={i18n.language === 'ar' ? 'الاسم الكامل' : 'Full Name'}
                value={formData.fullName}
                onChange={handleChange('fullName')}
                required
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                fullWidth
                label={i18n.language === 'ar' ? 'الهاتف' : 'Phone'}
                value={formData.phone}
                onChange={handleChange('phone')}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                fullWidth
                label={i18n.language === 'ar' ? 'البريد الإلكتروني' : 'Email'}
                type="email"
                value={formData.email}
                onChange={handleChange('email')}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <FormControl fullWidth>
                <InputLabel>{i18n.language === 'ar' ? 'المنطقة' : 'Region'}</InputLabel>
                <Select
                  value={formData.regionId}
                  onChange={handleChange('regionId')}
                  label={i18n.language === 'ar' ? 'المنطقة' : 'Region'}
                >
                  {(regions || []).map((region: any) => (
                    <MenuItem key={region.id} value={region.id}>
                      {region.governorate} - {region.city}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid size={{ xs: 12 }}>
              <FormControl fullWidth>
                <InputLabel>{i18n.language === 'ar' ? 'المجالات' : 'Fields'}</InputLabel>
                <Select
                  multiple
                  value={formData.fieldIds}
                  onChange={handleChange('fieldIds')}
                  label={i18n.language === 'ar' ? 'المجالات' : 'Fields'}
                  renderValue={(selected) => (
                    <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                      {(selected as number[]).map((value) => {
                        const field = (fields || []).find((f: any) => f.id === value);
                        return <Chip key={value} label={field?.name} size="small" />;
                      })}
                    </Box>
                  )}
                >
                  {(fields || []).map((field: any) => (
                    <MenuItem key={field.id} value={field.id}>
                      {field.name}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid size={{ xs: 12 }}>
              <TextField
                fullWidth
                label={i18n.language === 'ar' ? 'الملخص' : 'Summary'}
                value={formData.summary}
                onChange={handleChange('summary')}
                multiline
                rows={2}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 4 }}>
              <TextField
                fullWidth
                label={i18n.language === 'ar' ? 'سنوات الخبرة' : 'Experience Years'}
                type="number"
                value={formData.experienceYears}
                onChange={handleChange('experienceYears')}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 4 }}>
              <TextField
                fullWidth
                label={i18n.language === 'ar' ? 'سعر الخدمة' : 'Service Fee'}
                type="number"
                value={formData.baseServiceFee}
                onChange={handleChange('baseServiceFee')}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 4 }}>
              <FormControl fullWidth>
                <InputLabel>{i18n.language === 'ar' ? 'العملة' : 'Currency'}</InputLabel>
                <Select
                  value={formData.currency}
                  onChange={handleChange('currency')}
                  label={i18n.language === 'ar' ? 'العملة' : 'Currency'}
                >
                  <MenuItem value="EGP">EGP</MenuItem>
                  <MenuItem value="USD">USD</MenuItem>
                  <MenuItem value="EUR">EUR</MenuItem>
                </Select>
              </FormControl>
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
            disabled={createMutation.isPending}
          >
            {createMutation.isPending ? (
              <CircularProgress size={20} />
            ) : editingTech ? (
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
