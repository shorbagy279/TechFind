import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { Link, useNavigate } from 'react-router-dom';
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
  Avatar,
  Tabs,
  Tab,
  Divider
} from '@mui/material';
import { Add, Edit, Delete, Visibility, Build, LocationOn, Category } from '@mui/icons-material';
import client from '../../api/client';

interface Technician {
  id: number;
  fullName: string;
  phone: string;
  email: string;
  region: {
    id: number;
    governorate: string;
    city: string;
  };
  fields: Array<{ id: number; name: string }>;
  averageRating: number;
  totalReviews: number;
  active: boolean;
  availabilityStatus: string;
}

export default function AdminDashboard() {
  const { t, i18n } = useTranslation();
  const navigate = useNavigate();
  const qc = useQueryClient();
  
  const [currentTab, setCurrentTab] = useState(0);
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
    // Check if regions and fields exist
    if (!regions || regions.length === 0) {
      alert(i18n.language === 'ar' 
        ? 'يرجى إضافة مناطق أولاً من قائمة المناطق'
        : 'Please add regions first from the Regions tab');
      return;
    }
    if (!fields || fields.length === 0) {
      alert(i18n.language === 'ar'
        ? 'يرجى إضافة مجالات أولاً من قائمة المجالات'
        : 'Please add fields first from the Fields tab');
      return;
    }

    if (tech) {
      setEditingTech(tech);
      setFormData({
        fullName: tech.fullName,
        phone: tech.phone,
        email: tech.email,
        summary: '',
        regionId: tech.region?.id?.toString() || '',
        fieldIds: tech.fields?.map(f => f.id) || [],
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
    if (!formData.regionId) {
      setError(i18n.language === 'ar' ? 'المنطقة مطلوبة' : 'Region is required');
      return;
    }
    if (formData.fieldIds.length === 0) {
      setError(i18n.language === 'ar' ? 'يجب اختيار مجال واحد على الأقل' : 'At least one field is required');
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

  // Quick Stats Component
  const QuickStats = () => (
    <Grid container spacing={3} sx={{ mb: 4 }}>
      <Grid item xs={12} sm={6} md={3}>
        <Card sx={{ bgcolor: 'primary.main', color: 'white' }}>
          <CardContent>
            <Typography variant="h4">{technicians?.length || 0}</Typography>
            <Typography variant="body2">
              {i18n.language === 'ar' ? 'إجمالي الفنيين' : 'Total Technicians'}
            </Typography>
          </CardContent>
        </Card>
      </Grid>
      <Grid item xs={12} sm={6} md={3}>
        <Card sx={{ bgcolor: 'success.main', color: 'white' }}>
          <CardContent>
            <Typography variant="h4">{regions?.length || 0}</Typography>
            <Typography variant="body2">
              {i18n.language === 'ar' ? 'المناطق' : 'Regions'}
            </Typography>
          </CardContent>
        </Card>
      </Grid>
      <Grid item xs={12} sm={6} md={3}>
        <Card sx={{ bgcolor: 'warning.main', color: 'white' }}>
          <CardContent>
            <Typography variant="h4">{fields?.length || 0}</Typography>
            <Typography variant="body2">
              {i18n.language === 'ar' ? 'المجالات' : 'Fields'}
            </Typography>
          </CardContent>
        </Card>
      </Grid>
      <Grid item xs={12} sm={6} md={3}>
        <Card sx={{ bgcolor: 'info.main', color: 'white' }}>
          <CardContent>
            <Typography variant="h4">
              {technicians?.filter((t: Technician) => t.active)?.length || 0}
            </Typography>
            <Typography variant="body2">
              {i18n.language === 'ar' ? 'نشط' : 'Active'}
            </Typography>
          </CardContent>
        </Card>
      </Grid>
    </Grid>
  );

  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      {/* Header */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" gutterBottom sx={{ fontWeight: 'bold' }}>
          {i18n.language === 'ar' ? 'لوحة التحكم الإدارية' : 'Admin Dashboard'}
        </Typography>
        <Typography variant="body1" color="text.secondary">
          {i18n.language === 'ar' 
            ? 'إدارة الفنيين والمناطق والمجالات'
            : 'Manage technicians, regions, and fields'}
        </Typography>
      </Box>

      {/* Quick Stats */}
      <QuickStats />

      {/* Quick Actions */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            {i18n.language === 'ar' ? 'إجراءات سريعة' : 'Quick Actions'}
          </Typography>
          <Grid container spacing={2}>
            <Grid item xs={12} sm={4}>
              <Button
                fullWidth
                variant="outlined"
                startIcon={<Build />}
                onClick={() => navigate('/admin/fields')}
                size="large"
              >
                {i18n.language === 'ar' ? 'إدارة المجالات' : 'Manage Fields'}
              </Button>
            </Grid>
            <Grid item xs={12} sm={4}>
              <Button
                fullWidth
                variant="outlined"
                startIcon={<LocationOn />}
                onClick={() => navigate('/admin/regions')}
                size="large"
              >
                {i18n.language === 'ar' ? 'إدارة المناطق' : 'Manage Regions'}
              </Button>
            </Grid>
            <Grid item xs={12} sm={4}>
              <Button
                fullWidth
                variant="contained"
                startIcon={<Add />}
                onClick={() => handleOpenDialog()}
                size="large"
              >
                {i18n.language === 'ar' ? 'إضافة فني جديد' : 'Add New Technician'}
              </Button>
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {/* Warning if no regions or fields */}
      {(!regions || regions.length === 0 || !fields || fields.length === 0) && (
        <Alert severity="warning" sx={{ mb: 3 }}>
          <Typography variant="body2" gutterBottom>
            <strong>
              {i18n.language === 'ar' 
                ? '⚠️ إعداد مطلوب!' 
                : '⚠️ Setup Required!'}
            </strong>
          </Typography>
          {(!regions || regions.length === 0) && (
            <Typography variant="body2">
              • {i18n.language === 'ar' 
                ? 'لا توجد مناطق. الرجاء إضافة مناطق أولاً.'
                : 'No regions found. Please add regions first.'}
              <Button 
                size="small" 
                onClick={() => navigate('/admin/regions')}
                sx={{ ml: 1 }}
              >
                {i18n.language === 'ar' ? 'إضافة مناطق' : 'Add Regions'}
              </Button>
            </Typography>
          )}
          {(!fields || fields.length === 0) && (
            <Typography variant="body2">
              • {i18n.language === 'ar'
                ? 'لا توجد مجالات. الرجاء إضافة مجالات أولاً.'
                : 'No fields found. Please add fields first.'}
              <Button 
                size="small" 
                onClick={() => navigate('/admin/fields')}
                sx={{ ml: 1 }}
              >
                {i18n.language === 'ar' ? 'إضافة مجالات' : 'Add Fields'}
              </Button>
            </Typography>
          )}
        </Alert>
      )}

      {/* Technicians Table */}
      <Card>
        <CardContent>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
            <Typography variant="h6">
              {i18n.language === 'ar' ? 'قائمة الفنيين' : 'Technicians List'}
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
                      <Box sx={{ py: 4 }}>
                        <Typography color="text.secondary" gutterBottom>
                          {i18n.language === 'ar' ? 'لا يوجد فنيين' : 'No technicians yet'}
                        </Typography>
                        <Button
                          variant="contained"
                          startIcon={<Add />}
                          onClick={() => handleOpenDialog()}
                          sx={{ mt: 2 }}
                        >
                          {i18n.language === 'ar' ? 'إضافة أول فني' : 'Add First Technician'}
                        </Button>
                      </Box>
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
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label={i18n.language === 'ar' ? 'الاسم الكامل *' : 'Full Name *'}
                value={formData.fullName}
                onChange={handleChange('fullName')}
                required
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label={i18n.language === 'ar' ? 'الهاتف' : 'Phone'}
                value={formData.phone}
                onChange={handleChange('phone')}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label={i18n.language === 'ar' ? 'البريد الإلكتروني' : 'Email'}
                type="email"
                value={formData.email}
                onChange={handleChange('email')}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth required>
                <InputLabel>{i18n.language === 'ar' ? 'المنطقة *' : 'Region *'}</InputLabel>
                <Select
                  value={formData.regionId}
                  onChange={handleChange('regionId')}
                  label={i18n.language === 'ar' ? 'المنطقة *' : 'Region *'}
                >
                  {(regions || []).map((region: any) => (
                    <MenuItem key={region.id} value={region.id}>
                      {region.governorate} - {region.city}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12}>
              <FormControl fullWidth required>
                <InputLabel>{i18n.language === 'ar' ? 'المجالات *' : 'Fields *'}</InputLabel>
                <Select
                  multiple
                  value={formData.fieldIds}
                  onChange={handleChange('fieldIds')}
                  label={i18n.language === 'ar' ? 'المجالات *' : 'Fields *'}
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
            <Grid item xs={12}>
              <TextField
                fullWidth
                label={i18n.language === 'ar' ? 'الملخص' : 'Summary'}
                value={formData.summary}
                onChange={handleChange('summary')}
                multiline
                rows={2}
              />
            </Grid>
            <Grid item xs={12} sm={4}>
              <TextField
                fullWidth
                label={i18n.language === 'ar' ? 'سنوات الخبرة' : 'Experience Years'}
                type="number"
                value={formData.experienceYears}
                onChange={handleChange('experienceYears')}
              />
            </Grid>
            <Grid item xs={12} sm={4}>
              <TextField
                fullWidth
                label={i18n.language === 'ar' ? 'سعر الخدمة' : 'Service Fee'}
                type="number"
                value={formData.baseServiceFee}
                onChange={handleChange('baseServiceFee')}
              />
            </Grid>
            <Grid item xs={12} sm={4}>
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